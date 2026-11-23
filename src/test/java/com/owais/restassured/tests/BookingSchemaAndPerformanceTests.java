package com.owais.restassured.tests;

import com.owais.restassured.base.BaseTest;
import com.owais.restassured.config.Config;
import com.owais.restassured.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** JSON schema validation, content-type checks and response-time assertions. */
@Epic("Bookings")
@Feature("Schema and performance")
public class BookingSchemaAndPerformanceTests extends BaseTest {

    @Test(description = "GET /booking/{id} response matches booking-schema.json")
    @Severity(SeverityLevel.CRITICAL)
    public void getBooking_responseMatchesSchema() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.getBookingById(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"));
    }

    @Test(description = "GET /booking list response matches bookings-list-schema.json")
    @Severity(SeverityLevel.NORMAL)
    public void getAllBookings_responseMatchesSchema() {
        BOOKINGS.getAllBookings()
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/bookings-list-schema.json"));
    }

    @Test(description = "POST /booking response body matches the create-booking schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("The create response is {bookingid, booking}, so the schema is "
            + "asserted against the whole response. Validating only the nested "
            + "'booking' path with matchesJsonSchemaInClasspath fails in "
            + "RestAssured even when the data is valid, so the whole-response "
            + "schema is used here instead.")
    public void createBooking_responseMatchesSchema() {
        BOOKINGS.createBooking(TestDataFactory.defaultBooking())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/create-booking-schema.json"));
    }

    @Test(description = "GET /booking returns JSON content type")
    @Severity(SeverityLevel.NORMAL)
    public void getAllBookings_returnsJsonContentType() {
        Response response = BOOKINGS.getAllBookings();
        assertEquals(response.getStatusCode(), 200);
        assertTrue(response.getContentType().contains("application/json"),
                "Content-Type must be JSON, was: " + response.getContentType());
    }

    @Test(description = "POST /booking completes within the configured time limit")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_responseTimeUnderLimit() {
        long timeMs = BOOKINGS.createBooking(TestDataFactory.defaultBooking())
                .then()
                .statusCode(200)
                .extract()
                .time();
        assertResponseTimeUnderLimit(timeMs);
    }

    @Test(description = "GET /booking completes within the configured time limit")
    @Severity(SeverityLevel.NORMAL)
    public void getAllBookings_responseTimeUnderLimit() {
        long timeMs = BOOKINGS.getAllBookings()
                .then()
                .statusCode(200)
                .extract()
                .time();
        assertResponseTimeUnderLimit(timeMs);
    }

    @Test(description = "GET /booking/{id} completes within the configured time limit")
    @Severity(SeverityLevel.NORMAL)
    public void getBookingById_responseTimeUnderLimit() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());
        long timeMs = BOOKINGS.getBookingById(id)
                .then()
                .statusCode(200)
                .extract()
                .time();
        assertResponseTimeUnderLimit(timeMs);
        assertTrue(timeMs >= 0, "Response time must be measurable");
        LOG.info("GET /booking/{} took {} ms (limit {} ms)", id, timeMs, Config.maxResponseTimeMs());
    }
}
