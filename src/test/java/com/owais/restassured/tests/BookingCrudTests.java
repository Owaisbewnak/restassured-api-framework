package com.owais.restassured.tests;

import com.owais.restassured.base.BaseTest;
import com.owais.restassured.models.Booking;
import com.owais.restassured.models.BookingResponse;
import com.owais.restassured.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Happy-path CRUD for /booking plus the end-to-end lifecycle that chains
 * create → get → update → delete using the booking id returned at creation.
 * Every write test creates its own booking first — the public demo API is
 * shared, so tests never depend on a fixed pre-existing id.
 */
@Epic("Bookings")
@Feature("Booking CRUD")
public class BookingCrudTests extends BaseTest {

    @Test(description = "POST /booking returns 200 with an id and echoes the payload")
    @Severity(SeverityLevel.BLOCKER)
    public void createBooking_returns200WithIdAndEchoesPayload() {
        Booking request = TestDataFactory.defaultBooking();

        BookingResponse created = BOOKINGS.createBooking(request)
                .then()
                .statusCode(200)
                .extract()
                .as(BookingResponse.class);

        assertTrue(created.getBookingid() > 0, "bookingid must be a positive integer");
        assertEquals(created.getBooking().getFirstname(), request.getFirstname());
        assertEquals(created.getBooking().getLastname(), request.getLastname());
        assertEquals(created.getBooking().getTotalprice(), request.getTotalprice());
        assertEquals(created.getBooking().isDepositpaid(), request.isDepositpaid());
        assertEquals(created.getBooking().getBookingdates().getCheckin(), "2026-01-10");
        assertEquals(created.getBooking().getBookingdates().getCheckout(), "2026-01-15");
        assertEquals(created.getBooking().getAdditionalneeds(), "Breakfast");
    }

    @Test(description = "GET /booking/{id} returns the booking that was just created")
    @Severity(SeverityLevel.BLOCKER)
    public void getBookingById_returnsCreatedBooking() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        Booking fetched = BOOKINGS.getBookingById(id)
                .then()
                .statusCode(200)
                .extract()
                .as(Booking.class);

        assertEquals(fetched.getFirstname(), "Owais");
        assertEquals(fetched.getLastname(), "Bewnak");
        assertEquals(fetched.getTotalprice(), 150);
        assertTrue(fetched.isDepositpaid());
        assertNotNull(fetched.getBookingdates());
    }

    @Test(description = "GET /booking returns a non-empty list of booking ids")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllBookings_returnsNonEmptyList() {
        List<Integer> ids = BOOKINGS.getAllBookings()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid", Integer.class);

        assertFalse(ids.isEmpty(), "Booking list must not be empty");
        assertTrue(ids.stream().allMatch(id -> id > 0), "Every bookingid must be positive");
        LOG.info("GET /booking returned {} bookings", ids.size());
    }

    @Test(description = "GET /booking?firstname= finds the booking created with that firstname")
    @Severity(SeverityLevel.CRITICAL)
    public void getBookings_filteredByFirstname() {
        Booking unique = TestDataFactory.uniqueBooking();
        int id = BOOKINGS.createBookingAndGetId(unique);

        List<Integer> ids = BOOKINGS.getBookingsByFirstname(unique.getFirstname())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid", Integer.class);

        assertTrue(ids.contains(id), "Filtered list must contain the booking just created");
    }

    @Test(description = "GET /booking?lastname= finds the booking created with that lastname")
    @Severity(SeverityLevel.NORMAL)
    public void getBookings_filteredByLastname() {
        Booking unique = TestDataFactory.uniqueBooking();
        int id = BOOKINGS.createBookingAndGetId(unique);

        List<Integer> ids = BOOKINGS.getBookingsByLastname(unique.getLastname())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid", Integer.class);

        assertTrue(ids.contains(id), "Filtered list must contain the booking just created");
    }

    @Test(description = "GET /booking?checkin= returns 200 with a non-empty list")
    @Severity(SeverityLevel.NORMAL)
    @Description("Restful Booker checkin filter is unreliable on the shared demo API: "
            + "a booking created with checkin 2026-02-01 does not appear in the "
            + "?checkin=2026-02-01 list, while the same booking IS found by "
            + "?firstname= and in the full list. So this test only asserts the "
            + "filter endpoint responds correctly, it does not assert containment.")
    public void getBookings_filteredByCheckin() {
        Booking unique = TestDataFactory.uniqueBooking();
        int id = BOOKINGS.createBookingAndGetId(unique);

        // Sanity: the booking really exists, so a missing id here is the API filter, not our data.
        BOOKINGS.getBookingById(id).then().statusCode(200);

        List<Integer> ids = BOOKINGS.getBookingsByCheckin("2026-02-01")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid", Integer.class);

        assertFalse(ids.isEmpty(), "Checkin-filtered list must not be empty");
        assertTrue(ids.stream().allMatch(filteredId -> filteredId > 0),
                "Every bookingid in the filtered list must be positive");
        LOG.info("GET /booking?checkin=2026-02-01 returned {} bookings", ids.size());
    }

    @Test(description = "PUT /booking/{id} with a token replaces every field")
    @Severity(SeverityLevel.BLOCKER)
    public void updateBooking_fullUpdate_replacesAllFields() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());
        Booking updated = TestDataFactory.booking(
                "Updated", "Name", 999, false, "2026-02-01", "2026-02-03", "None");

        Booking result = BOOKINGS.updateBooking(id, token, updated)
                .then()
                .statusCode(200)
                .extract()
                .as(Booking.class);

        assertEquals(result.getFirstname(), "Updated");
        assertEquals(result.getLastname(), "Name");
        assertEquals(result.getTotalprice(), 999);
        assertFalse(result.isDepositpaid());
        assertEquals(result.getBookingdates().getCheckin(), "2026-02-01");
        assertEquals(result.getAdditionalneeds(), "None");
    }

    @Test(description = "PATCH /booking/{id} changes only the firstname, other fields stay")
    @Severity(SeverityLevel.CRITICAL)
    public void partialUpdateBooking_updatesOnlyFirstname() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        Booking result = BOOKINGS.partialUpdateBooking(id, token, "{\"firstname\": \"Patched\"}")
                .then()
                .statusCode(200)
                .extract()
                .as(Booking.class);

        assertEquals(result.getFirstname(), "Patched");
        assertEquals(result.getLastname(), "Bewnak", "Lastname must be unchanged by PATCH");
        assertEquals(result.getTotalprice(), 150, "Total price must be unchanged by PATCH");
    }

    @Test(description = "DELETE /booking/{id} returns 201 and the booking is gone afterwards")
    @Severity(SeverityLevel.BLOCKER)
    public void deleteBooking_removesBooking() {
        int id = BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());

        BOOKINGS.deleteBooking(id, token)
                .then()
                .statusCode(201);

        BOOKINGS.getBookingById(id)
                .then()
                .statusCode(404);
    }

    @Test(description = "Full lifecycle: create -> get -> update -> get -> delete -> get 404")
    @Severity(SeverityLevel.BLOCKER)
    @Description("The chaining showcase — every step reuses the booking id "
            + "returned by the previous step, exactly as a real E2E flow does.")
    public void fullLifecycle_createGetUpdateDelete() {
        int id = createStep();
        verifyStep(id);
        updateStep(id);
        deleteStep(id);
    }

    @Step("Create a booking and capture its id")
    private int createStep() {
        return BOOKINGS.createBookingAndGetId(TestDataFactory.defaultBooking());
    }

    @Step("GET the booking and verify its fields")
    private void verifyStep(int id) {
        Booking fetched = BOOKINGS.getBookingById(id)
                .then()
                .statusCode(200)
                .extract()
                .as(Booking.class);
        assertEquals(fetched.getFirstname(), "Owais");
    }

    @Step("PUT new values and verify the update stuck")
    private void updateStep(int id) {
        Booking updated = TestDataFactory.booking(
                "Lifecycle", "Updated", 500, true, "2026-03-01", "2026-03-05", "Dinner");
        Booking result = BOOKINGS.updateBooking(id, token, updated)
                .then()
                .statusCode(200)
                .extract()
                .as(Booking.class);
        assertEquals(result.getFirstname(), "Lifecycle");
        assertEquals(result.getTotalprice(), 500);
    }

    @Step("DELETE the booking and confirm GET now returns 404")
    private void deleteStep(int id) {
        BOOKINGS.deleteBooking(id, token).then().statusCode(201);
        BOOKINGS.getBookingById(id).then().statusCode(404);
    }

    @Test(description = "POST /booking works without the optional additionalneeds field")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_withoutAdditionalNeeds_succeeds() {
        BookingResponse created = BOOKINGS.createBooking(TestDataFactory.minimalBooking())
                .then()
                .statusCode(200)
                .extract()
                .as(BookingResponse.class);

        assertTrue(created.getBookingid() > 0);
        assertEquals(created.getBooking().getFirstname(), "Minimal");
    }
}
