package com.quick_park_assist.serviceImplTest;


import com.quick_park_assist.entity.ParkingSpot;
import com.quick_park_assist.repository.ParkingSpotRepository;
import com.quick_park_assist.serviceImpl.ParkingSpotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ParkingSpotServiceImplTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @InjectMocks
    private ParkingSpotServiceImpl parkingSpotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAvailableSpots_WithValidSearchQuery() {
        // Arrange
        String searchQuery = "Zone A";
        List<ParkingSpot> mockSpots = new ArrayList<>();
        mockSpots.add(new ParkingSpot());
        mockSpots.add(new ParkingSpot());

        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery))
                .thenReturn(mockSpots);

        // Act
        List<ParkingSpot> result = parkingSpotService.getAllAvailableSpots(searchQuery);

        // Assert
        assertEquals(2, result.size());
        verify(parkingSpotRepository, times(1)).findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery);
    }

    @Test
    void testGetAllAvailableSpots_WithNoMatchingSpots() {
        // Arrange
        String searchQuery = "Nonexistent Location";
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery))
                .thenReturn(new ArrayList<>());

        // Act
        List<ParkingSpot> result = parkingSpotService.getAllAvailableSpots(searchQuery);

        // Assert
        assertEquals(0, result.size());
        verify(parkingSpotRepository, times(1)).findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery);
    }

    @Test
    void testGetAllAvailableSpots_WithEmptySearchQuery() {
        // Arrange
        String searchQuery = "";
        List<ParkingSpot> mockSpots = new ArrayList<>();
        mockSpots.add(new ParkingSpot());

        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery))
                .thenReturn(mockSpots);

        // Act
        List<ParkingSpot> result = parkingSpotService.getAllAvailableSpots(searchQuery);

        // Assert
        assertEquals(1, result.size());
        verify(parkingSpotRepository, times(1)).findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery);
    }

    @Test
    void testGetAllAvailableSpots_WithNullSearchQuery() {
        // Arrange
        String searchQuery = null;
        List<ParkingSpot> mockSpots = new ArrayList<>();
        mockSpots.add(new ParkingSpot());

        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery))
                .thenReturn(mockSpots);

        // Act
        List<ParkingSpot> result = parkingSpotService.getAllAvailableSpots(searchQuery);

        // Assert
        assertEquals(1, result.size());
        verify(parkingSpotRepository, times(1)).findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "available", searchQuery, searchQuery);
    }
}

