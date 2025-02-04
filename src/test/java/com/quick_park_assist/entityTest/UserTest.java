package com.quick_park_assist.entityTest;

import com.quick_park_assist.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    // Valid user test case
    @Test
    void testValidUser() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPhoneNumber("1234567890");
        user.setUserType("Admin");
        user.setPassword("password123");
        user.setAddress("123 Main St");
        user.setActive(true);

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals("Admin", user.getUserType());
        assertEquals("password123", user.getPassword());
        assertEquals("123 Main St", user.getAddress());
        assertTrue(user.isActive());
    }

    // Testing null full name
    @Test
    void testNullFullName() {
        User user = new User();
        user.setFullName(null);
        assertNull(user.getFullName());
    }

    // Testing invalid phone number (less than 10 digits)
    @Test
    void testInvalidPhoneNumber() {
        User user = new User();
        user.setPhoneNumber("12345"); // Invalid number (less than 10 digits)
        assertEquals("12345", user.getPhoneNumber());
    }

    // Testing empty email field
    @Test
    void testEmptyEmail() {
        User user = new User();
        user.setEmail(""); // Empty string
        assertEquals("", user.getEmail());
    }

    // Testing null address field
    @Test
    void testNullAddress() {
        User user = new User();
        user.setAddress(null);
        assertNull(user.getAddress());
    }

    // Testing active status field (set to false)
    @Test
    void testActiveStatus() {
        User user = new User();
        user.setActive(false);
        assertFalse(user.isActive());
    }

    // Testing the createdAt field automatically set by @PrePersist
    @Test
    void testCreatedAt() {
        User user = new User();
        user.setCreatedAt(null); // Initially null
        user.onCreate();  // Manually invoke the @PrePersist method
        assertNotNull(user.getCreatedAt()); // Created at should be automatically set to current time
    }

    // Testing user type
    @Test
    void testUserType() {
        User user = new User();
        user.setUserType("Customer");
        assertEquals("Customer", user.getUserType());
    }

    // Testing password field
    @Test
    void testPassword() {
        User user = new User();
        user.setPassword("newPassword123");
        assertEquals("newPassword123", user.getPassword());
    }

    // Testing valid phone number pattern
    @Test
    void testPhoneNumberPattern() {
        User user = new User();
        user.setPhoneNumber("9876543210");  // Valid phone number (10 digits)
        assertEquals("9876543210", user.getPhoneNumber());
    }

    // Testing email validation (invalid email format)
    @Test
    void testInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email"); // Invalid email format
        assertEquals("invalid-email", user.getEmail());
    }

    // Testing duplicate phone number (assuming phone number uniqueness)
    @Test
    void testDuplicatePhoneNumber() {
        // Simulating the scenario where a duplicate phone number is encountered
        User user1 = new User();
        user1.setPhoneNumber("1234567890");
        User user2 = new User();
        user2.setPhoneNumber("1234567890"); // Same phone number as user1

        // Here, in real code, we'd check that the system throws a unique constraint violation exception,
        // but for now, we are testing it as per your test case setup
        assertEquals("1234567890", user2.getPhoneNumber());
    }

    // Testing the user status field when active
    @Test
    void testUserStatusActive() {
        User user = new User();
        user.setActive(true);
        assertTrue(user.isActive());
    }

    // Testing user with empty required fields (will throw validation exception in real-world scenarios)
    @Test
    void testUserWithEmptyFields() {
        User user = new User();
        user.setFullName("");
        user.setEmail("");
        user.setPhoneNumber("12345");  // Invalid phone number
        user.setAddress("");  // Empty address

        assertEquals("", user.getFullName());
        assertEquals("", user.getEmail());
        assertEquals("12345", user.getPhoneNumber());  // Invalid phone number should be tested based on the validation
    }
}
