package com.quick_park_assist.serviceImplTest;

import com.quick_park_assist.repository.BookingSpotRepository;
import com.quick_park_assist.repository.VehicleRepository;
import com.quick_park_assist.repository.ParkingSpotRepository;
import com.quick_park_assist.repository.ReservationRepository;
import com.quick_park_assist.serviceImpl.StatsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceImplTest {

    @Mock
    private BookingSpotRepository bookingSpotRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStatsForUser() {
        // Arrange
        Long userId = 1L;
        when(bookingSpotRepository.countByUserId(userId)).thenReturn(5L);
        when(bookingSpotRepository.sumDurationByUserId(userId)).thenReturn(12L);
        when(bookingSpotRepository.sumEstimatedPriceByUserId(userId)).thenReturn(100.0);
        when(vehicleRepository.countByUserId(userId)).thenReturn(2L);

        // Act
        Map<String, Object> stats = statsService.getStatsForUser(userId);

        // Assert
        assertEquals(5L, stats.get("availableSpots"));
        assertEquals(12L, stats.get("totalHours"));
        assertEquals(100.0, stats.get("amountSpent"));
        assertEquals(2L, stats.get("activeBookings"));
    }

    @Test
    void testGetStatsForUser_NoData() {
        // Arrange
        Long userId = 2L;
        when(bookingSpotRepository.countByUserId(userId)).thenReturn(0L);
        when(bookingSpotRepository.sumDurationByUserId(userId)).thenReturn(0L);
        when(bookingSpotRepository.sumEstimatedPriceByUserId(userId)).thenReturn(0.0);
        when(vehicleRepository.countByUserId(userId)).thenReturn(0L);

        // Act
        Map<String, Object> stats = statsService.getStatsForUser(userId);

        // Assert
        assertEquals(0L, stats.get("availableSpots"));
        assertEquals(0L, stats.get("totalHours"));
        assertEquals(0.0, stats.get("amountSpent"));
        assertEquals(0L, stats.get("activeBookings"));
    }

    @Test
    void testGetStatsForUser_Exception() {
        // Arrange
        Long userId = 1L;
        when(bookingSpotRepository.countByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            statsService.getStatsForUser(userId);
        } catch (Exception e) {
            assertEquals("Database error", e.getMessage());
        }
    }

    @Test
    void testGetSpotOwnerStats() {
        // Arrange
        Long userId = 1L;
        when(parkingSpotRepository.countTotalSpotsByOwner(userId)).thenReturn(10);
        when(parkingSpotRepository.countEvSpotsByOwner(userId)).thenReturn(5);

        // Act
        Map<String, Object> stats = statsService.getSpotOwnerStats(userId);


    }

    @Test
    void testGetSpotOwnerStats_NoData() {
        // Arrange
        Long userId = 2L;
        when(parkingSpotRepository.countTotalSpotsByOwner(userId)).thenReturn(0);
        when(parkingSpotRepository.countEvSpotsByOwner(userId)).thenReturn(0);

        // Act
        Map<String, Object> stats = statsService.getSpotOwnerStats(userId);

    }

    @Test
    void testGetSpotOwnerStats_Exception() {
        // Arrange
        Long userId = 1L;
        when(parkingSpotRepository.countTotalSpotsByOwner(userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            statsService.getSpotOwnerStats(userId);
        } catch (Exception e) {
            assertEquals("Database error", e.getMessage());
        }
    }

    @Test
    void testGetRecentActivity() {
        // Arrange
        Long userId = 1L;
        int limit = 5;

        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("bookingId", 1L);
        activity1.put("spotId", 10L);

        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("bookingId", 2L);
        activity2.put("spotId", 11L);

        List<Map<String, Object>> recentActivity = Arrays.asList(activity1, activity2);
        Page<Map<String, Object>> page = new PageImpl<>(recentActivity);
        when(bookingSpotRepository.findRecentActivityByUserId(eq(userId), any(PageRequest.class))).thenReturn(page);

        // Act
        List<Map<String, Object>> result = statsService.getRecentActivityForUser(userId, limit);

        // Assert
        assertEquals(2, result.size());
        assertEquals(activity1, result.get(0));
        assertEquals(activity2, result.get(1));
    }

    @Test
    void testGetRecentActivity_NoData() {
        // Arrange
        Long userId = 1L;
        int limit = 5;
        Page<Map<String, Object>> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingSpotRepository.findRecentActivityByUserId(eq(userId), any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        List<Map<String, Object>> result = statsService.getRecentActivityForUser(userId, limit);

        // Assert
        assertEquals(0, result.size());
    }
    @Test
    void testGetRecentActivityForOwner_Success() {
        Long userId = 1L;
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit);

        List<Map<String, Object>> parkingSpotList = new ArrayList<>();
        Map<String, Object> spot1 = new HashMap<>();
        spot1.put("id", 101L);
        spot1.put("spotName", "A1");
        spot1.put("spotType", "Compact");
        spot1.put("Location", "Downtown");
        spot1.put("pricePerHour", 5.0);
        parkingSpotList.add(spot1);

        Map<String, Object> spot2 = new HashMap<>();
        spot2.put("id", 102L);
        spot2.put("spotName", "B2");
        spot2.put("spotType", "SUV");
        spot2.put("Location", "Uptown");
        spot2.put("pricePerHour", 7.5);
        parkingSpotList.add(spot2);

        Page<Map<String, Object>> parkingSpotPage = new PageImpl<>(parkingSpotList, pageable, parkingSpotList.size());

        when(parkingSpotRepository.getRecentActivityByUserId(userId, pageable)).thenReturn(parkingSpotPage);

        List<Map<String, Object>> result = statsService.getRecentActivityForOwner(userId, limit);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).get("id"));
        assertEquals("A1", result.get(0).get("spotName"));
        assertEquals(102L, result.get(1).get("id"));
    }

    @Test
    void testGetRecentActivityForOwner_EmptyResult() {
        Long userId = 2L;
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);

        Page<Map<String, Object>> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(parkingSpotRepository.getRecentActivityByUserId(userId, pageable)).thenReturn(emptyPage);

        List<Map<String, Object>> result = statsService.getRecentActivityForOwner(userId, limit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
