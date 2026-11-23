package com.owais.restassured.clients;

import com.owais.restassured.config.Config;
import com.owais.restassured.models.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper around the /booking endpoints. Every method returns the raw
 * RestAssured {@link Response} so tests own their assertions — the client
 * only knows how to build and send requests, including auth.
 *
 * <p>Restful Booker accepts the token as a {@code Cookie: token=...} header
 * (and also as Basic auth). Cookie auth is used here because it is the
 * documented mechanism for this API.</p>
 */
public class BookingClient {

    private static final Logger LOG = LogManager.getLogger(BookingClient.class);

    private RequestSpecification base() {
        return given()
                .baseUri(Config.baseUrl())
                .contentType("application/json")
                .accept("application/json");
    }

    private RequestSpecification authed(String token) {
        return base().header("Cookie", "token=" + token);
    }

    public Response getAllBookings() {
        Response response = base().when().get("/booking");
        LOG.info("GET /booking -> {}", response.getStatusCode());
        return response;
    }

    public Response getBookingsByFirstname(String firstname) {
        return base().queryParam("firstname", firstname).when().get("/booking");
    }

    public Response getBookingsByLastname(String lastname) {
        return base().queryParam("lastname", lastname).when().get("/booking");
    }

    public Response getBookingsByCheckin(String checkin) {
        return base().queryParam("checkin", checkin).when().get("/booking");
    }

    public Response getBookingsByCheckout(String checkout) {
        return base().queryParam("checkout", checkout).when().get("/booking");
    }

    public Response getBookingById(int bookingId) {
        Response response = base().when().get("/booking/" + bookingId);
        LOG.info("GET /booking/{} -> {}", bookingId, response.getStatusCode());
        return response;
    }

    public Response createBooking(Booking booking) {
        Response response = base().body(booking).when().post("/booking");
        LOG.info("POST /booking -> {}", response.getStatusCode());
        return response;
    }

    /** Creates a booking and returns its id, or throws if creation failed. */
    public int createBookingAndGetId(Booking booking) {
        int id = createBooking(booking)
                .then()
                .statusCode(200)
                .extract()
                .path("bookingid");
        LOG.info("Created booking id={}", id);
        return id;
    }

    public Response updateBooking(int bookingId, String token, Booking booking) {
        return authed(token).body(booking).when().put("/booking/" + bookingId);
    }

    public Response partialUpdateBooking(int bookingId, String token, String jsonPatch) {
        return authed(token).body(jsonPatch).when().patch("/booking/" + bookingId);
    }

    public Response deleteBooking(int bookingId, String token) {
        Response response = authed(token).when().delete("/booking/" + bookingId);
        LOG.info("DELETE /booking/{} -> {}", bookingId, response.getStatusCode());
        return response;
    }

    /** Sends a raw JSON body — used by negative tests (malformed / incomplete payloads). */
    public Response createBookingRaw(String rawJson) {
        return base().body(rawJson).when().post("/booking");
    }

    public Response updateBookingWithoutAuth(int bookingId, Booking booking) {
        return base().body(booking).when().put("/booking/" + bookingId);
    }

    public Response deleteBookingWithoutAuth(int bookingId) {
        return base().when().delete("/booking/" + bookingId);
    }
}
