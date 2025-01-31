package com.quick_park_assist.entityTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quick_park_assist.entity.User;
import com.quick_park_assist.entity.Vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

public class VehicleTests {

    private Vehicle vehicle;

    @BeforeEach
    public void setUp() {
        vehicle = new Vehicle();
        vehicle.onCreate(); // Call the onCreate() method here
    }

    @Test
    public void testId() {
        vehicle.setId(1L);
        assertEquals(1L, vehicle.getId());
    }

    @Test
    public void testUser() {
        User user = new User();
        user.setId(1L);  // Set an ID for the user
        vehicle.setUser(user);
        assertEquals(user, vehicle.getUser());
    }

    @Test
    public void testVehicleNumber() {
        vehicle.setVehicleNumber("ABC123");
        assertEquals("ABC123", vehicle.getVehicleNumber());
    }

    @Test
    public void testVehicleType() {
        vehicle.setVehicleType("Sedan");
        assertEquals("Sedan", vehicle.getVehicleType());
    }

    @Test
    public void testManufacturer() {
        vehicle.setManufacturer("Toyota");
        assertEquals("Toyota", vehicle.getManufacturer());
    }

    @Test
    public void testModel() {
        vehicle.setModel("Camry");
        assertEquals("Camry", vehicle.getModel());
    }

    @Test
    public void testColor() {
        vehicle.setColor("Red");
        assertEquals("Red", vehicle.getColor());
    }

    @Test
    public void testRegisteredAt() {
        LocalDateTime now = LocalDateTime.now();
        vehicle.setRegisteredAt(now);
        assertEquals(now, vehicle.getRegisteredAt());
    }

    @Test
    public void testRegisteredAtAfterOnCreate() {
        LocalDateTime registeredAt = vehicle.getRegisteredAt();
        LocalDateTime now = LocalDateTime.now();
        
        // Check that registeredAt is close to now
        assertEquals(registeredAt.getYear(), now.getYear());
        assertEquals(registeredAt.getMonth(), now.getMonth());
        assertEquals(registeredAt.getDayOfMonth(), now.getDayOfMonth());
        assertEquals(registeredAt.getHour(), now.getHour());
        assertEquals(registeredAt.getMinute(), now.getMinute());
        // You might want to check seconds or milliseconds if precision is important
    }

    @Test
    public void testEv() {
        vehicle.setEv(true);
        assertEquals(true, vehicle.isEv());
    }
}
