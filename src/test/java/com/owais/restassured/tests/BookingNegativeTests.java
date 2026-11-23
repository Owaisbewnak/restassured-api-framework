package com.owais.restassured.tests;

import com.owais.restassured.base.BaseTest;
import com.owais.restassured.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Negative cases. Every expected status below was verified against the live
 * API on 2026-11-22 — including two quirks: write operations on a
 * non-existent id return 405 (not 404), and creating a booking with a
 * missing required field returns 500 (not 400).
 */
@Epic("Bookings")
@Feature("Booking negative cases")
public class BookingNegativeTests extends BaseTest {

    @Test(description = "GET /booking/{non-existent id} returns 404")
    @Severity(SeverityLevel.CRITICAL)
    public void getBooking_nonExistentId_returns404() {
        BOOKINGS.getBookingById(999999999)
                .then()
                .statusCode(404);
    }

    @Test(description = "GET /booking/{non-numeric id} returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void getBooking_nonNumericId_returns404() {
        io.restassured.RestAssured.given()
                .baseUri(com.owais.restassured.config.Config.baseUrl())
                .when()
                .get("/booking/abc")
                .then()
                .statusCode(404);
    }

    @Test(description = "PUT without a token returns 403 Forbidden")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBooking_withoutAuth_returns403() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.updateBookingWithoutAuth(id, TestDataFactory.defaultBooking())
                .then()
                .statusCode(403);
    }

    @Test(description = "DELETE without a token returns 403 Forbidden")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteBooking_withoutAuth_returns403() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.deleteBookingWithoutAuth(id)
                .then()
                .statusCode(403);
    }

    @Test(description = "PUT with an invalid token returns 403 Forbidden")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBooking_withInvalidToken_returns403() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.updateBooking(id, "invalidtoken123", TestDataFactory.defaultBooking())
                .then()
                .statusCode(403);
    }

    @Test(description = "DELETE with an invalid token returns 403 Forbidden")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteBooking_withInvalidToken_returns403() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.deleteBooking(id, "invalidtoken123")
                .then()
                .statusCode(403);
    }

    @Test(description = "POST /booking without firstname returns 500")
    @Severity(SeverityLevel.NORMAL)
    @Description("The API returns 500 for a missing required field — asserted as "
            + "observed, not as the 400 a well-designed API would return.")
    public void createBooking_missingFirstname_returns500() {
        BOOKINGS.createBookingRaw(
                        "{\"lastname\":\"X\",\"totalprice\":100,\"depositpaid\":true,"
                                + "\"bookingdates\":{\"checkin\":\"2026-01-10\",\"checkout\":\"2026-01-15\"}}")
                .then()
                .statusCode(500);
    }

    @Test(description = "POST /booking without bookingdates returns 500")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_missingBookingDates_returns500() {
        BOOKINGS.createBookingRaw(
                        "{\"firstname\":\"A\",\"lastname\":\"B\",\"totalprice\":100,\"depositpaid\":true}")
                .then()
                .statusCode(500);
    }

    @Test(description = "PUT on a non-existent id with a valid token returns 405")
    @Severity(SeverityLevel.NORMAL)
    public void updateBooking_nonExistentId_returns405() {
        BOOKINGS.updateBooking(999999999, token, TestDataFactory.defaultBooking())
                .then()
                .statusCode(405);
    }

    @Test(description = "DELETE on a non-existent id with a valid token returns 405")
    @Severity(SeverityLevel.NORMAL)
    public void deleteBooking_nonExistentId_returns405() {
        BOOKINGS.deleteBooking(999999999, token)
                .then()
                .statusCode(405);
    }
}
