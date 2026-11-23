package com.owais.restassured.clients;

import com.owais.restassured.config.Config;
import com.owais.restassured.models.AuthRequest;
import com.owais.restassured.models.AuthResponse;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/** Thin wrapper around POST /auth. Returns the raw Response so tests can assert on it. */
public class AuthClient {

    private static final Logger LOG = LogManager.getLogger(AuthClient.class);

    public Response createToken(String username, String password) {
        AuthRequest body = new AuthRequest(username, password);
        Response response = given()
                .baseUri(Config.baseUrl())
                .contentType("application/json")
                .accept("application/json")
                .body(body)
                .when()
                .post("/auth");
        LOG.info("POST /auth username={} -> {}", username, response.getStatusCode());
        return response;
    }

    public Response createTokenWithDefaultCredentials() {
        return createToken(Config.username(), Config.password());
    }

    /** Convenience for tests that only need a valid token string. */
    public String getToken() {
        AuthResponse auth = createTokenWithDefaultCredentials()
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);
        if (auth.getToken() == null || auth.getToken().isBlank()) {
            throw new IllegalStateException("Auth token was empty in /auth response");
        }
        return auth.getToken();
    }
}
