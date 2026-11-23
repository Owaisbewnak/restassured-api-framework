package com.owais.restassured.tests;

import com.owais.restassured.base.BaseTest;
import com.owais.restassured.dataproviders.BookingDataProvider;
import com.owais.restassured.models.Booking;
import com.owais.restassured.models.BookingResponse;
import com.owais.restassured.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Data-driven creation tests: one test method, many datasets — five bookings
 * loaded from {@code testdata/bookings.json} and four price combinations
 * from an inline DataProvider. Each created booking is deleted afterwards
 * so the shared demo API is left clean.
 */
@Epic("Bookings")
@Feature("Data-driven booking creation")
public class BookingDataDrivenTests extends BaseTest {

    @Test(dataProvider = "bookingsFromJson",
            dataProviderClass = BookingDataProvider.class,
            description = "Create each booking from testdata/bookings.json and verify the echo")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Five datasets from JSON: different names, prices (including 0 and 999), "
            + "deposit flags and a booking with no additionalneeds.")
    public void createBooking_fromJsonData_succeeds(Booking booking) {
        BookingResponse created = BOOKINGS.createBooking(booking)
                .then()
                .statusCode(200)
                .extract()
                .as(BookingResponse.class);

        assertTrue(created.getBookingid() > 0, "bookingid must be positive");
        assertEquals(created.getBooking().getFirstname(), booking.getFirstname());
        assertEquals(created.getBooking().getLastname(), booking.getLastname());
        assertEquals(created.getBooking().getTotalprice(), booking.getTotalprice());
        assertEquals(created.getBooking().isDepositpaid(), booking.isDepositpaid());

        // Cleanup: remove the booking we just created.
        BOOKINGS.deleteBooking(created.getBookingid(), token).then().statusCode(201);
    }

    @Test(dataProvider = "bookingPrices",
            dataProviderClass = BookingDataProvider.class,
            description = "Create bookings with different totalprice values and verify the price sticks")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_withVariousPrices_succeeds(
            String firstname, String lastname, int totalprice, boolean depositpaid) {
        Booking booking = TestDataFactory.booking(
                firstname, lastname, totalprice, depositpaid,
                "2026-09-01", "2026-09-03", "Data-driven price test");

        BookingResponse created = BOOKINGS.createBooking(booking)
                .then()
                .statusCode(200)
                .extract()
                .as(BookingResponse.class);

        assertEquals(created.getBooking().getTotalprice(), totalprice,
                "API must store the totalprice that was sent");
        assertEquals(created.getBooking().getFirstname(), firstname);

        BOOKINGS.deleteBooking(created.getBookingid(), token).then().statusCode(201);
    }
}
