package com.owais.restassured.tests;

import com.owais.restassured.base.BaseTest;
import com.owais.restassured.config.Config;
import com.owais.restassured.dataproviders.BookingDataProvider;
import com.owais.restassured.models.AuthResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * POST /auth — token creation.
 *
 * <p>Verified API behaviour (probed 2026-11-22): valid credentials return
 * {@code 200 + {"token": "..."}}; <b>invalid credentials also return HTTP 200</b>
 * but with {@code {"reason": "Bad credentials"}} and no token. Tests assert
 * that quirk explicitly instead of assuming a 403.</p>
 */
@Epic("Auth")
@Feature("POST /auth")
public class AuthTests extends BaseTest {

    @Test(description = "Valid credentials return 200 and a non-empty token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /auth with admin / password123 must return a token.")
    public void validCredentials_returnsToken() {
        Response response = AUTH.createTokenWithDefaultCredentials();

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 for valid credentials");
        AuthResponse auth = response.as(AuthResponse.class);
        assertNotNull(auth.getToken(), "Token must be present");
        assertFalse(auth.getToken().isBlank(), "Token must not be blank");
        LOG.info("Token received ({} chars)", auth.getToken().length());
    }

    @Test(description = "Auth response matches the JSON schema")
    @Severity(SeverityLevel.NORMAL)
    public void validCredentials_responseMatchesSchema() {
        AUTH.createTokenWithDefaultCredentials()
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth-schema.json"));
    }

    @Test(dataProvider = "invalidAuthCredentials",
            dataProviderClass = BookingDataProvider.class,
            description = "Invalid credentials return 200 with 'Bad credentials' and no token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Restful Booker returns HTTP 200 + reason for bad credentials; "
            + "the test asserts no token is issued.")
    public void invalidCredentials_returnBadCredentialsWithoutToken(String username, String password) {
        Response response = AUTH.createToken(username, password);

        assertEquals(response.getStatusCode(), 200,
                "API returns 200 even for bad credentials (verified behaviour)");
        assertEquals(response.jsonPath().getString("reason"), "Bad credentials");
        assertNull(response.jsonPath().getString("token"),
                "No token must be issued for invalid credentials");
    }

    @Test(description = "Auth with an empty JSON body returns 'Bad credentials'")
    @Severity(SeverityLevel.NORMAL)
    public void emptyBody_returnsBadCredentials() {
        Response response = io.restassured.RestAssured.given()
                .baseUri(Config.baseUrl())
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/auth");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.jsonPath().getString("reason"), "Bad credentials");
        assertTrue(response.getBody().asString().contains("Bad credentials"));
    }
}
