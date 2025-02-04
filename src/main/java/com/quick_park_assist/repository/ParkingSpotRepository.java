package com.quick_park_assist.repository;

import com.quick_park_assist.entity.ParkingSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.user.id = :userId")
    int countTotalSpotsByOwner(Long userId);

    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.user.id = :userId AND p.spotType = 'EV_SPOT'")
    int countEvSpotsByOwner(Long userId);

    // 1. Find by location (case-insensitive) and spotType not equal to "EV_SPOT"
    List<ParkingSpot> findByLocationContainingIgnoreCase(String location);

    // 2. Find by location, availability, and spotType not equal to "EV_SPOT"
    List<ParkingSpot> findByLocationContainingIgnoreCaseAndAvailabilityIgnoreCase(
            String location, String availability);

    // 3. Find by availability and spotLocation or location (both case-insensitive), and spotType not equal to "EV_SPOT"
    List<ParkingSpot> findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String availability, String spotLocation, String location);
    boolean existsParkingSpotBySpotLocationIgnoreCaseAndLocationIgnoreCase(String spotLocation,String Location);
    List<ParkingSpot> findByUserId(Long userId);
    List<ParkingSpot> findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCaseAndSpotType(
            String availability, String spotLocation, String location, String spotType);

    List<ParkingSpot> findBySpotType(String evSpot);


    @Query("SELECT new map(p.id as id, p.spotLocation as spotName, p.spotType as spotType, p.location as Location, p.pricePerHour as pricePerHour) " +
            "FROM ParkingSpot p WHERE p.user.id = :userId ")
    Page<Map<String, Object>> getRecentActivityByUserId(@Param("userId")Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);
}