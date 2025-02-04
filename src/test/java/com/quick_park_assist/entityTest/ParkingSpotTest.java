package com.quick_park_assist.entityTest;
import static org.junit.jupiter.api.Assertions.*;

import com.quick_park_assist.entity.ParkingSpot;
import com.quick_park_assist.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

 class ParkingSpotTest {

    private ParkingSpot parkingSpot;
    private User user;

    @BeforeEach
    public void setUp() {
        parkingSpot = new ParkingSpot();
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
    }

    @Test
    public void testSetAndGetId() {
        parkingSpot.setId(1L);
        assertEquals(1L, parkingSpot.getId());
    }

    @Test
    public void testSetAndGetLocation() {
        String location = "Main Street";
        parkingSpot.setLocation(location);
        assertEquals(location, parkingSpot.getLocation());
    }

    @Test
    public void testSetAndGetSpotLocation() {
        String spotLocation = "Lot 42";
        parkingSpot.setSpotLocation(spotLocation);
        assertEquals(spotLocation, parkingSpot.getSpotLocation());
    }

    @Test
    public void testSetAndGetAvailability() {
        String availability = "Available";
        parkingSpot.setAvailability(availability);
        assertEquals(availability, parkingSpot.getAvailability());
    }

    @Test
    public void testSetAndGetPricePerHour() {
        double pricePerHour = 10.5;
        parkingSpot.setPricePerHour(pricePerHour);
        assertEquals(pricePerHour, parkingSpot.getPricePerHour());
    }

    @Test
    public void testSetAndGetSpotType() {
        String spotType = "Compact";
        parkingSpot.setSpotType(spotType);
        assertEquals(spotType, parkingSpot.getSpotType());
    }

    @Test
    public void testSetAndGetUser() {
        parkingSpot.setUser(user);
        assertEquals(user, parkingSpot.getUser());
    }

    @Test
    public void testSetAndGetAdditionalInstructions() {
        String instructions = "Park near the entrance.";
        parkingSpot.setAdditionalInstructions(instructions);
        assertEquals(instructions, parkingSpot.getAdditionalInstructions());
    }

     @Test
     void testToString() {
         ParkingSpot parkingSpot = new ParkingSpot();
         parkingSpot.setId(1L);
         parkingSpot.setUser(user);
         parkingSpot.setLocation("Downtown");
         parkingSpot.setSpotLocation("A1");
         parkingSpot.setAvailability("Available");
         parkingSpot.setPricePerHour(10.0);
         parkingSpot.setSpotType("Compact");
         parkingSpot.setAdditionalInstructions("Near entrance");

         String expectedString = "ParkingSpot{" +
                 "id=1'user_id'User{id=1, fullName='Test User', email='null', phoneNumber='null', userType='null', address='null', createdAt=null, active=false}'" +
                 ", location='Downtown'" +
                 ", spotLocation='A1'" +
                 ", availability='Available'" +
                 ", pricePerHour=10.0" +
                 ", spotType='Compact'" +
                 ", additionalInstructions='Near entrance'" +
                 '}';

         assertEquals(expectedString, parkingSpot.toString());
     }

@Test
public void testDefaultValues() {
    assertNull(parkingSpot.getId());
    assertNull(parkingSpot.getLocation());
    assertNull(parkingSpot.getSpotLocation());
    assertNull(parkingSpot.getAvailability());
    assertEquals(0.0, parkingSpot.getPricePerHour());
    assertNull(parkingSpot.getSpotType());
    assertNull(parkingSpot.getUser());
    assertNull(parkingSpot.getAdditionalInstructions());
}
}
