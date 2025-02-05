package com.quick_park_assist.controllerTest;

import com.quick_park_assist.controller.ReservationController;
import com.quick_park_assist.entity.ParkingSpot;
import com.quick_park_assist.entity.Reservation;
import com.quick_park_assist.entity.User;
import com.quick_park_assist.repository.ParkingSpotRepository;
import com.quick_park_assist.repository.UserRepository;
import com.quick_park_assist.repository.VehicleRepository;
import com.quick_park_assist.service.IReservationService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationControllerTest {

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private IReservationService reservationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListReservations_UserNotLoggedIn() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);
        String result = reservationController.listReservations(session, model);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testListReservations_Success() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        List<Reservation> reservations = Arrays.asList(new Reservation());
        when(reservationService.getReservationsByUserId(USER_ID)).thenReturn(reservations);

        String result = reservationController.listReservations(session, model);
        assertEquals("ViewReservations", result);
        verify(model).addAttribute(ReservationController.RESERVATIONS, reservations);
    }

    

    @Test
    void testAddReservation_InvalidParameters() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date());
        String result = reservationController.addReservation("", "", session, reservation, redirectAttributes);
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }

    

    @Test
    void testEditForm_UserNotLoggedIn() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);
        String result = reservationController.editForm(session, model);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testEditForm_Success() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        List<Reservation> reservations = Arrays.asList(new Reservation());
        when(reservationService.getReservationsByUserId(USER_ID)).thenReturn(reservations);

        String result = reservationController.editForm(session, model);
        assertEquals("EditReservation", result);
        verify(model).addAttribute(ReservationController.RESERVATIONS, reservations);
    }

    

    @Test
    void testDeleteReservation_Success() {
        Long reservationId = 1L;
        when(reservationService.deleteReservationById(reservationId)).thenReturn(true);

        String result = reservationController.deleteReservation(reservationId, model, redirectAttributes);

        assertEquals("redirect:/ev-charging/delete-form", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.SUCCESS_MESSAGE, "Reservation Successfully Cancelled");
    }

    @Test
    void testDeleteReservation_Failure() {
        Long reservationId = 1L;
        when(reservationService.deleteReservationById(reservationId)).thenReturn(false);

        String result = reservationController.deleteReservation(reservationId, model, redirectAttributes);

        assertEquals("redirect:/ev-charging/delete-form", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Reservation Couldn't be Cancelled");
    }


    
    @Test
    void testUpdateSpotDetails_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);

        // Act
        String result = reservationController.updateSpotDetails(1L, "2023-01-01T12:00", "EV123", redirectAttributes, session, model);

        // Assert
        assertEquals("redirect:/login", result);
    }

    @Test
    void testUpdateSpotDetails_InvalidDate() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        
        // Act
        String result = reservationController.updateSpotDetails(1L, "invalid-date", "EV123", redirectAttributes, session, model);

        // Assert
        assertEquals("redirect:/ev-charging/edit", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Exception Invalid date format. Please use the correct format.");
    }
    @Test
    void testAddReservation_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);

        // Act
        String result = reservationController.addReservation("EV123", "1", session, new Reservation(), redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
    }

    @Test
    void testAddReservation_InvalidVehicleOrSpot() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();

        // Act
        String result = reservationController.addReservation("", "", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }

    @Test
    void testAddReservation_TimeInPast() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() - 1000)); // 1 second in the past

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Please Choose Correct Date and time");
    }

    @Test
    void testAddReservation_InvalidSpotFormat() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000)); // 10 seconds in the future

        // Act
        String result = reservationController.addReservation("EV123", "invalid-spot", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Error in selecting the SPOT");
    }

    @Test
    void testAddReservation_SpotNotFound() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.empty());
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000)); // 10 seconds in the future

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Error in selecting the SPOT");
    }

    @Test
    void testAddReservation_VehicleNotRegisteredOrNotEV() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000)); // 10 seconds in the future
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.of(new ParkingSpot())); // Mock a spot
        when(vehicleRepository.existsVehicleByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(false); // Not an EV

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "The Vehicle Number is not Registered or not an EV");
    }

    @Test
    void testAddReservation_TimeSlotAlreadyBooked() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000)); // 10 seconds in the future
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.of(new ParkingSpot())); // Mock a spot
        when(vehicleRepository.existsVehicleByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true); // Valid vehicle
        when(reservationService.isTimeSlotAvailable(any(Date.class), anyString())).thenReturn(false); // Slot is booked

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "The selected time slot is already booked.");
    }

    @Test
    void testAddReservation_Successful() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000)); // 10 seconds in the future
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.of(new ParkingSpot())); // Mock a spot
        when(vehicleRepository.existsVehicleByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true); // Valid vehicle
        when(reservationService.isTimeSlotAvailable(any(Date.class), anyString())).thenReturn(true); // Slot is available
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User())); // Mock a user

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.SUCCESS_MESSAGE, "Reservation is Successful!");
    }
    
    
    
    
    @Test
    void testAddReservationForm_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
    }

    @Test
    void testAddReservationForm_NoEV() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsElectricVehicleByUserId(USER_ID)).thenReturn(false);

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/vehicles/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "You currently Don't have an EV to Reserve a spot");
    }

    

    @Test
    void testAddReservationForm_NoEVSpotsAvailable() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsElectricVehicleByUserId(USER_ID)).thenReturn(true);
        
        when(parkingSpotRepository.findBySpotType("EV_SPOT")).thenReturn(Collections.emptyList());

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("addReservation", result);
        verify(model).addAttribute("evSpots", Collections.emptyList());
        verify(vehicleRepository).findEVVehicles(USER_ID);
        // Optionally verify that log info was called if you have a logger
    }
    

    

    @Test
    void testAddReservationForm_UserDoesNotHaveEV() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsElectricVehicleByUserId(USER_ID)).thenReturn(false);

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/vehicles/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "You currently Don't have an EV to Reserve a spot");
    }
    @Test
    void testShowDeleteForm_UserLoggedIn() {
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        List<Reservation> reservations = Arrays.asList(new Reservation());
        when(reservationService.getReservationsByUserId(USER_ID)).thenReturn(reservations);

        String result = reservationController.showDeleteForm(session, model);
        
        assertEquals("CancelReservation", result);
        verify(model).addAttribute(ReservationController.RESERVATIONS, reservations);
    }

    @Test
    void testShowDeleteForm_UserNotLoggedIn() {
        // Arrange: Set up the session to return null for the USER_ID
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(null);

        // Act: Call the showDeleteForm method
        String result = reservationController.showDeleteForm(session, model);

        // Assert: Verify that the redirect to login occurs
        assertEquals("redirect:/login", result);
    }
    @Test
    void testUpdateSpotDetails_Success() throws Exception {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true);
        when(reservationService.updateSpotDetails(anyLong(), any(Date.class), anyString())).thenReturn(true);

        // Use a valid date string
        String validDateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date(System.currentTimeMillis() + 3600000)); // 1 hour in the future

        // Act
        String result = reservationController.updateSpotDetails(1L, validDateStr, "EV123", redirectAttributes, session, model);

        // Assert
        assertEquals("redirect:/ev-charging/edit", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.SUCCESS_MESSAGE, "Booking updated successfully!");
    }
    @Test
    void testUpdateSpotDetails_DateInPast() throws Exception {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true);
        String pastDateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date(System.currentTimeMillis() - 3600000)); // 1 hour in the past

        // Act
        String result = reservationController.updateSpotDetails(1L, pastDateStr, "EV123", redirectAttributes, session, model);

        // Assert
        assertEquals("redirect:/ev-charging/edit", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid date format. Please use the correct format.");
    }
    @Test
    void testUpdateSpotDetails_VehicleNotEV() throws Exception {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(false);
        String validDateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date(System.currentTimeMillis() + 3600000)); // 1 hour in the future

        // Act
        String result = reservationController.updateSpotDetails(1L, validDateStr, "EV123", redirectAttributes, session, model);

        // Assert
        assertEquals("redirect:/ev-charging/edit", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "The Vehicle is not an EV");
    }
    @Test
    void testUpdateSpotDetails_UpdateFailed() throws Exception {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true);
        when(reservationService.updateSpotDetails(anyLong(), any(Date.class), anyString())).thenReturn(false);
        String validDateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date(System.currentTimeMillis() + 3600000)); // 1 hour in the future

        // Act
        String result = reservationController.updateSpotDetails(999L, validDateStr, "EV123", redirectAttributes, session, model); // Using a non-existing ID

        // Assert
        assertEquals("redirect:/ev-charging/edit", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Update failed. Booking ID not found.");
    }
    

    @Test
    void testAddReservationForm_NoEvSpotsAvailable() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsElectricVehicleByUserId(USER_ID)).thenReturn(true);
        when(parkingSpotRepository.findBySpotType("EV_SPOT")).thenReturn(Collections.emptyList()); // No EV spots available

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("addReservation", result); // Verify that it still returns the correct view
        // Check if log message is generated for no available spots (consider using a logger mock if needed)
    }

    @Test
    void testAddReservationForm_EvVehiclesFetched() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(vehicleRepository.existsElectricVehicleByUserId(USER_ID)).thenReturn(true);
        when(parkingSpotRepository.findBySpotType("EV_SPOT")).thenReturn(Arrays.asList(new ParkingSpot())); // Mock EV spots
        when(vehicleRepository.findEVVehicles(USER_ID)).thenReturn(Arrays.asList("EV123", "EV456")); // Mock multiple EV vehicles

        // Act
        String result = reservationController.addReservationForm(session, model, redirectAttributes);

        // Assert
        assertEquals("addReservation", result); // Verify that it returns the correct view
        verify(model).addAttribute("evVehicles", Arrays.asList("EV123", "EV456")); // Verify that multiple EV vehicle details were added to the model
    }
    
    @Test
    void testAddReservation_NullVehicle() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();

        // Act
        String result = reservationController.addReservation(null, "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }

    @Test
    void testAddReservation_EmptyVehicle() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();

        // Act
        String result = reservationController.addReservation("", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }

    @Test
    void testAddReservation_NullSpot() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();

        // Act
        String result = reservationController.addReservation("EV123", null, session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }

    @Test
    void testAddReservation_EmptySpot() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        Reservation reservation = new Reservation();

        // Act
        String result = reservationController.addReservation("EV123", "", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "Invalid vehicle or spot selection.");
    }
    
    @Test
    void testAddReservation_ExceptionDuringReservation() {
        // Arrange
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.of(new ParkingSpot())); 
        when(vehicleRepository.existsVehicleByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true);
        when(reservationService.isTimeSlotAvailable(any(Date.class), anyString())).thenReturn(true);
        
        User user = new User();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // Simulate exception during reservation
        doThrow(new RuntimeException("Database error")).when(reservationService).addReservation(any(Reservation.class));

        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis() + 10000));

        // Act
        String result = reservationController.addReservation("EV123", "1", session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(redirectAttributes).addFlashAttribute(ReservationController.ERROR_MESSAGE, "The Vehicle Number is not Registered or not an EV");
        verify(reservationService).addReservation(any(Reservation.class));
    }

    @Test
    void testAddReservation_UserNotFoundExceptionHandled() {
        // Arrange
        Long userId = 1L;
        when(session.getAttribute(ReservationController.USER_ID)).thenReturn(USER_ID);
        when(parkingSpotRepository.findById(anyLong())).thenReturn(Optional.of(new ParkingSpot()));
        when(vehicleRepository.existsVehicleByVehicleNumberAndUserIdAndEvTrue("EV123", USER_ID)).thenReturn(true);
        when(reservationService.isTimeSlotAvailable(any(Date.class), anyString())).thenReturn(true);

        Reservation reservation = new Reservation();
        reservation.setReservationTime(new Date(System.currentTimeMillis()+3600*1000));// Replace with your actual Reservation class
        reservation.setStatus("PENDING");

        // Simulate userRepository throwing a RuntimeException
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("User not found"));

        // Act
        String result = reservationController.addReservation("EV123","1",session, reservation, redirectAttributes);

        // Assert
        assertEquals("redirect:/ev-charging/add", result);
        verify(reservationService).addReservation(reservation);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Reservation is Successful!");
        assertNull(reservation.getUser(), "User should be null after exception.");
    }


}
