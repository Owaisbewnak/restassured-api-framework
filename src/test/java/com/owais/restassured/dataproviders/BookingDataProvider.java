package com.owais.restassured.dataproviders;

import com.owais.restassured.models.Booking;
import com.owais.restassured.utils.JsonUtils;
import org.testng.annotations.DataProvider;

import java.util.List;

/** All DataProviders for the suite — JSON-file driven and inline. */
public class BookingDataProvider {

    @DataProvider(name = "bookingsFromJson")
    public Object[][] bookingsFromJson() {
        List<Booking> bookings = JsonUtils.readBookings("testdata/bookings.json");
        Object[][] data = new Object[bookings.size()][1];
        for (int i = 0; i < bookings.size(); i++) {
            data[i][0] = bookings.get(i);
        }
        return data;
    }

    @DataProvider(name = "bookingPrices")
    public Object[][] bookingPrices() {
        return new Object[][]{
                {"PriceZero", "Guest", 0, true},
                {"PriceTypical", "Guest", 150, true},
                {"PriceHigh", "Guest", 9999, false},
                {"PriceNegative", "Guest", -50, true}
        };
    }

    @DataProvider(name = "invalidAuthCredentials")
    public Object[][] invalidAuthCredentials() {
        return new Object[][]{
                {"admin", "wrongpassword"},
                {"wronguser", "password123"},
                {"", "password123"},
                {"admin", ""},
                {"", ""}
        };
    }
}
