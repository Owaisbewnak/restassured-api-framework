package com.owais.restassured.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owais.restassured.models.Booking;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Reads test data from JSON files under src/test/resources/testdata/. */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static List<Booking> readBookings(String resourcePath) {
        try (InputStream in = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Test data not found: " + resourcePath);
            }
            return MAPPER.readValue(in, new TypeReference<List<Booking>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read test data: " + resourcePath, e);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialise object to JSON", e);
        }
    }
}
