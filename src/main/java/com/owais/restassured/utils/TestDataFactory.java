package com.owais.restassured.utils;

import com.owais.restassured.models.Booking;
import com.owais.restassured.models.BookingDates;

import java.util.UUID;

/** Builds valid Booking payloads. A random suffix keeps names unique on the shared public API. */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Booking defaultBooking() {
        return new Booking(
                "Owais",
                "Bewnak",
                150,
                true,
                new BookingDates("2026-01-10", "2026-01-15"),
                "Breakfast");
    }

    public static Booking booking(String firstname, String lastname, int totalprice,
                                  boolean depositpaid, String checkin, String checkout,
                                  String additionalneeds) {
        return new Booking(firstname, lastname, totalprice, depositpaid,
                new BookingDates(checkin, checkout), additionalneeds);
    }

    /** A booking with a unique firstname so filter tests can find exactly their own record. */
    public static Booking uniqueBooking() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new Booking(
                "Test" + suffix,
                "User" + suffix,
                200,
                true,
                new BookingDates("2026-02-01", "2026-02-05"),
                "Late checkout");
    }

    /** Minimal valid booking — no additionalneeds (Jackson omits the null field). */
    public static Booking minimalBooking() {
        return new Booking(
                "Minimal",
                "Guest",
                50,
                false,
                new BookingDates("2026-03-01", "2026-03-02"),
                null);
    }
}
