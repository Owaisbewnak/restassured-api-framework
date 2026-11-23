package com.owais.restassured.base;

import com.owais.restassured.clients.AuthClient;
import com.owais.restassured.clients.BookingClient;
import com.owais.restassured.config.Config;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.ProxySpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;

import java.net.URI;

/**
 * Shared setup for every test class: base URI, request/response logging and
 * one authenticated token per suite run (Restful Booker tokens are reusable
 * across calls, so a single token is fetched once and shared).
 */
public abstract class BaseTest {

    protected static final Logger LOG = LogManager.getLogger(BaseTest.class);

    protected static final AuthClient AUTH = new AuthClient();
    protected static final BookingClient BOOKINGS = new BookingClient();

    /** Auth token fetched once per suite; refreshed lazily if a test needs a new one. */
    protected static String token;

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        RestAssured.baseURI = Config.baseUrl();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        configureProxyIfPresent();
        token = AUTH.getToken();
        LOG.info("Suite setup complete. Base URL: {}", Config.baseUrl());
    }

    /**
     * Opt-in proxy support: if {@code https_proxy} / {@code HTTPS_PROXY}
     * (or the {@code http_proxy} variants) is set in the environment — as in
     * many corporate networks and CI sandboxes — route RestAssured traffic
     * through it, with auth when the URL carries credentials. With no proxy
     * variable set this is a no-op, so a plain local run needs nothing extra.
     */
    private void configureProxyIfPresent() {
        String proxyUrl = firstNonBlank(
                System.getenv("https_proxy"), System.getenv("HTTPS_PROXY"),
                System.getenv("http_proxy"), System.getenv("HTTP_PROXY"));
        if (proxyUrl == null) {
            return;
        }
        try {
            URI uri = new URI(proxyUrl);
            ProxySpecification spec = ProxySpecification.host(uri.getHost())
                    .withPort(uri.getPort() == -1 ? 8080 : uri.getPort());
            if (uri.getUserInfo() != null && uri.getUserInfo().contains(":")) {
                String[] parts = uri.getUserInfo().split(":", 2);
                spec = spec.withAuth(parts[0], parts[1]);
            }
            RestAssured.proxy(spec);
            LOG.info("Proxy configured: {}:{}", uri.getHost(), uri.getPort());
        } catch (Exception e) {
            LOG.warn("Could not parse proxy URL, continuing without a proxy", e);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Step("Fetch a fresh auth token")
    protected String freshToken() {
        return AUTH.getToken();
    }

    @Step("Assert response time is under the configured limit")
    protected void assertResponseTimeUnderLimit(long actualMs) {
        long limit = Config.maxResponseTimeMs();
        LOG.info("Response time: {} ms (limit {} ms)", actualMs, limit);
        org.testng.Assert.assertTrue(actualMs < limit,
                "Response took " + actualMs + " ms, over the " + limit + " ms limit");
    }
}
