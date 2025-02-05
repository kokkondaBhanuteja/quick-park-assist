package com.quick_park_assist.controllerTest;

import com.quick_park_assist.controller.StatsController;
import com.quick_park_assist.service.IStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsControllerTest {

    private StatsController statsController;
    private IStatsService statsService;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        statsService = Mockito.mock(IStatsService.class);
        statsController = new StatsController(statsService);
        session = Mockito.mock(HttpSession.class);
    }

    // Test cases for getStatsForUser
    @Test
    void testGetStatsForUser_Unauthorized_NoUserId() {
        when(session.getAttribute("userId")).thenReturn(null);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_Unauthorized_NoUserType() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_Forbidden_InvalidUserType() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("ADMIN");

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_VehicleOwner_Success() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("key", "value");
        when(statsService.getStatsForUser(1L)).thenReturn(mockStats);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockStats, response.getBody());
    }

    @Test
    void testGetStatsForUser_SpotOwner_Success() {
        when(session.getAttribute("userId")).thenReturn(2L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        Map<String, Object> mockSpotOwnerStats = new HashMap<>();
        mockSpotOwnerStats.put("spot", "data");
        when(statsService.getSpotOwnerStats(2L)).thenReturn(mockSpotOwnerStats);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSpotOwnerStats, response.getBody());
    }

    // Test cases for getRecentActivity
    @Test
    void testGetRecentActivity_Unauthorized_NoUserId() {
        when(session.getAttribute("userId")).thenReturn(null);

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetRecentActivity_Unauthorized_InvalidUserIdType() {
        when(session.getAttribute("userId")).thenReturn("notALong");

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetRecentActivity_Success() {
        when(session.getAttribute("userId")).thenReturn(3L);

        List<Map<String, Object>> mockRecentActivity = new ArrayList<>();
        Map<String, Object> activity = new HashMap<>();
        activity.put("activity", "data");
        mockRecentActivity.add(activity);

        when(statsService.getRecentActivityForUser(3L, 4)).thenReturn(mockRecentActivity);

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }
    @Test
    void testGetRecentActivity_SuccessOwner() {
        when(session.getAttribute("userId")).thenReturn(2L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        List<Map<String, Object>> mockRecentActivity = new ArrayList<>();
        Map<String, Object> activity = new HashMap<>();
        activity.put("activity", "data");
        mockRecentActivity.add(activity);

        when(statsService.getRecentActivityForOwner(3L, 4)).thenReturn(mockRecentActivity);

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }
    // Additional test cases for getStatsForUser
    @Test
    void testGetStatsForUser_NullSession() {

        when(session.getAttribute("userId")).thenReturn(null);
        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_BothUserIdAndUserTypeNull() {
        when(session.getAttribute("userId")).thenReturn(null);
        when(session.getAttribute("userType")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_EmptyUserType() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("");

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_UnhandledUserType() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("RANDOM_TYPE");

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_VehicleOwner_EmptyStats() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");
        when(statsService.getStatsForUser(1L)).thenReturn(new HashMap<>());

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new HashMap<>(), response.getBody());
    }

    @Test
    void testGetStatsForUser_VehicleOwner_NullStats() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");
        when(statsService.getStatsForUser(1L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetStatsForUser_SpotOwner_NegativeUserId() {
        when(session.getAttribute("userId")).thenReturn(-1L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        when(statsService.getSpotOwnerStats(-1L)).thenReturn(new HashMap<>());

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new HashMap<>(), response.getBody());
    }

    @Test
    void testGetStatsForUser_SpotOwner_NullStats() {
        when(session.getAttribute("userId")).thenReturn(2L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        when(statsService.getSpotOwnerStats(2L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = statsController.getStatsForUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    // Additional test cases for getRecentActivity
    @Test
    void testGetRecentActivity_NullSession() {
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testGetRecentActivity_EmptyRecentActivity() {
        when(session.getAttribute("userId")).thenReturn(3L);
        when(statsService.getRecentActivityForUser(3L, 4)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }
    @Test
    void testGetRecentActivity_EmptyRecentActivityForOwner() {
        when(session.getAttribute("userId")).thenReturn(3L);
        when(statsService.getRecentActivityForOwner(3L, 4)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }
    @Test
    void testGetRecentActivity_NullRecentActivityForUser() {
        when(session.getAttribute("userId")).thenReturn(3L);
        when(statsService.getRecentActivityForUser(3L, 4)).thenReturn(null);

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    @Test
    void testGetRecentActivity_NullRecentActivityForOwner() {
        when(session.getAttribute("userId")).thenReturn(3L);
        when(statsService.getRecentActivityForOwner(3L, 4)).thenReturn(null);

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    @Test
    void testGetRecentActivity_NegativeUserIdForUser() {
        when(session.getAttribute("userId")).thenReturn(-1L);
        when(statsService.getRecentActivityForUser(-1L, 4)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    @Test
    void testGetRecentActivity_NegativeUserIdForOwner() {
        when(session.getAttribute("userId")).thenReturn(-1L);
        when(statsService.getRecentActivityForOwner(-1L, 4)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
    }
    @Test
    void testGetRecentActivity_VehicleOwner() {
        Long mockUserId = 123L;
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("activity", "Some activity");
        mockData.add(data);

        // Mock session attributes
        when(session.getAttribute("userId")).thenReturn(mockUserId);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");

        // Mock service call
        when(statsService.getRecentActivityForUser(mockUserId, 4)).thenReturn(mockData);

        // Call the method
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Validate response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Some activity", response.getBody().get(0).get("activity"));
    }

    /*@Test
    void testGetRecentActivity_SpotOwner() {
        Long mockUserId = 456L;
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("activity", "Another activity");
        mockData.add(data);

        // Mock session attributes
        when(session.getAttribute("userId")).thenReturn(mockUserId);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");

        // Mock service call
        when(statsService.getRecentActivityForOwner(mockUserId, 4)).thenReturn(mockData);

        // Call the method
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Validate response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Another activity", response.getBody().get(0).get("activity"));
    }*/

    @Test
    void testGetRecentActivity_SpotOwner() {
        // Arrange
        Long userId = 1L;
        when(session.getAttribute("userId")).thenReturn(userId);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");

        List<Map<String, Object>> mockRecentActivity = Arrays.asList(
                Map.of("activity", "Booked a spot", "timestamp", "2025-02-01"),
                Map.of("activity", "Updated spot details", "timestamp", "2025-02-02")
        );

        when(statsService.getRecentActivityForOwner(userId, 4)).thenReturn(mockRecentActivity);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Assert
        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200");
        assertEquals(mockRecentActivity, response.getBody(), "Expected the recent activity for spot owner.");
    }

    @Test
    void testGetRecentActivity_UserTypeNull() {
        // Arrange
        Long userId = 1L;
        when(session.getAttribute("userId")).thenReturn(userId);
        when(session.getAttribute("userType")).thenReturn(null); // userType is null

        // Act
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Assert
        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200");
        assertTrue(response.getBody().isEmpty(), "Expected an empty list when userType is null.");
    }
    @Test
     void testGetRecentActivityForSpotOwner() {
        HttpSession session = mock(HttpSession.class);

        // Mock session attributes
        when(session.getAttribute("userId")).thenReturn(123L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");

        // Mock statsService method call
        List<Map<String, Object>> mockActivity = new ArrayList<>();
        Map<String, Object> activity = new HashMap<>();
        activity.put("activity", "Sample activity for SPOT_OWNER");
        mockActivity.add(activity);

        when(statsService.getRecentActivityForOwner(123L, 4)).thenReturn(mockActivity);

        // Call the method
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Sample activity for SPOT_OWNER", response.getBody().get(0).get("activity"));

        // Verify that the correct service method was called
        verify(statsService).getRecentActivityForOwner(123L, 4);
        verify(statsService, never()).getRecentActivityForUser(anyLong(), anyInt());
    }
    @Test
    void testGetRecentActivityForSpotOwnerIsNull() {
        HttpSession session = mock(HttpSession.class);

        // Mock session attributes
        when(session.getAttribute("userId")).thenReturn(123L);
        when(session.getAttribute("userType")).thenReturn(null);

        // Mock statsService method call
        List<Map<String, Object>> mockActivity = new ArrayList<>();
        Map<String, Object> activity = new HashMap<>();
        activity.put("activity", "Sample activity for SPOT_OWNER");
        mockActivity.add(activity);

        when(statsService.getRecentActivityForOwner(123L, 4)).thenReturn(mockActivity);

        // Call the method
        ResponseEntity<List<Map<String, Object>>> response = statsController.getRecentActivity(session);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

}
