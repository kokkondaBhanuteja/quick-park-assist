package com.quick_park_assist.entityTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quick_park_assist.entity.Reservation;
import com.quick_park_assist.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

public class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    public void setUp() {
        reservation = new Reservation();
    }

    @Test
    public void testId() {
        reservation.setId(1L);
        assertEquals(1L, reservation.getId());
    }

    @Test
    public void testName() {
        reservation.setName("John Doe");
        assertEquals("John Doe", reservation.getName());
    }

    @Test
    public void testVehicleNumber() {
        reservation.setVehicleNumber("ABC123");
        assertEquals("ABC123", reservation.getVehicleNumber());
    }

    @Test
    public void testChargingStation() {
        reservation.setChargingStation("Station 1");
        assertEquals("Station 1", reservation.getChargingStation());
    }

    @Test
    public void testUser() {
        User user = new User(); // Assuming a User class exists
        user.setId(1L);
        reservation.setUser(user);
        assertEquals(user, reservation.getUser());
    }

    @Test
    public void testSlot() {
        reservation.setSlot("A1");
        assertEquals("A1", reservation.getSlot());
    }

    @Test
    public void testStatus() {
        reservation.setStatus("Confirmed");
        assertEquals("Confirmed", reservation.getStatus());
    }

    @Test
    public void testReservationTime() {
        Date date = new Date();
        reservation.setReservationTime(date);
        assertEquals(date, reservation.getReservationTime());
    }

    // New test for spotId
    @Test
    public void testSpotId() {
        reservation.setSpotId(1L); // Set the spot ID
        assertEquals(1L, reservation.getSpotId()); // Assert that it matches
    }
}
