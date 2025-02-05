package com.quick_park_assist.controllerTest;

import com.quick_park_assist.controller.ParkingSpotController;
import com.quick_park_assist.entity.ParkingSpot;
import com.quick_park_assist.entity.User;
import com.quick_park_assist.repository.ParkingSpotRepository;
import com.quick_park_assist.repository.UserRepository;
import com.quick_park_assist.service.IUpdateParkingSpotService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ParkingSpotUnitTest {

    @InjectMocks
    private ParkingSpotController parkingSpotController;  // Assuming this is the controller name

    @Mock
    private HttpSession session;

    @Mock
    private IUpdateParkingSpotService updateParkingSpotService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    //Test 1

    @Test
    public void testShowAddSpotForm_UserNotLoggedIn_ShouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);  // No user in session

        // Act
        String viewName = parkingSpotController.showAddSpotForm(session, model);

        // Assert
        assertEquals("redirect:/login", viewName);
    }

    //Test 2

    @Test
    public void testShowAddSpotForm_UserIsSpotOwner_ShouldReturnAddParkingSpotView() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L); // Logged in user
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER"); // User type is spot owner

        // Act
        String viewName = parkingSpotController.showAddSpotForm(session, model);

        // Assert
        assertEquals("AddParkingSpot", viewName);  // Should return the AddParkingSpot view
        verify(model).addAttribute(eq("parkingSpot"), any(ParkingSpot.class)); // Verify model is populated with a new ParkingSpot
    }

    //Test 3

    @Test
    public void testShowAddSpotForm_UserIsNotSpotOwner_ShouldRedirectToSearch() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);  // Logged in user
        when(session.getAttribute("userType")).thenReturn("OTHER_TYPE"); // User is not a spot owner

        // Act
        String viewName = parkingSpotController.showAddSpotForm(session, model);

        // Assert
        assertEquals("redirect:/smart-spots/search", viewName);  // Should redirect to the search page
    }




    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    private Long loggedInUserId;
    private User user;
    private ParkingSpot parkingSpot;

    @BeforeEach
    public void setUpAdd() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        loggedInUserId = 1L;  // Sample logged-in user ID

        // Creating a mock user
        user = new User();
        user.setId(loggedInUserId);

        // Creating a mock parking spot
        parkingSpot = new ParkingSpot();
        parkingSpot.setSpotLocation("Location1");
        parkingSpot.setLocation("Address1");
    }

    //Test 4

    @Test
    public void testAddSpot_UserNotLoggedIn_ShouldRedirectToLogin() {


        ParkingSpot parkingSpot;

        parkingSpot = new ParkingSpot();
        parkingSpot.setSpotLocation("Location1");
        parkingSpot.setLocation("Address1");

        // Arrange: Mock session to return null for "userId"
        when(session.getAttribute("userId")).thenReturn(null);

        // Act: Call the controller method
        String viewName = parkingSpotController.addSpot(session, parkingSpot, model, redirectAttributes);

        // Assert: Verify that the user is redirected to login
        assertEquals("redirect:/login", viewName);
    }

    //Test 5

    @Test
    public void testAddSpot_UserExists_SpotAlreadyOwned_ShouldRedirectWithError() {
        // Arrange: Mock session to return loggedInUserId
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);

        // Mock user repository to return the mock user
        when(userRepository.findById(loggedInUserId)).thenReturn(Optional.of(user));

        // Mock parkingSpotRepository to indicate the parking spot already exists
        when(parkingSpotRepository.existsParkingSpotBySpotLocationIgnoreCaseAndLocationIgnoreCase("Location1", "Address1"))
                .thenReturn(true);

        // Act: Call the controller method
        String viewName = parkingSpotController.addSpot(session, parkingSpot, model, redirectAttributes);

        // Assert: Verify the redirect with the error message
        assertEquals("redirect:/smart-spots/add-spot", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "The Spot is Already Owned, Please Choose another Spot Location.");
    }

    //Test 6

    @Test
    public void testAddSpot_SuccessfulSpotAddition_ShouldRedirectWithSuccessMessage() {
        // Arrange: Mock session to return loggedInUserId
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);

        // Mock user repository to return the mock user
        when(userRepository.findById(loggedInUserId)).thenReturn(Optional.of(user));

        // Mock parkingSpotRepository to indicate the parking spot does not already exist
        when(parkingSpotRepository.existsParkingSpotBySpotLocationIgnoreCaseAndLocationIgnoreCase("Location1", "Address1"))
                .thenReturn(false);

        // Act: Call the controller method
        String viewName = parkingSpotController.addSpot(session, parkingSpot, model, redirectAttributes);

        // Assert: Verify the redirect with the success message
        assertEquals("redirect:/smart-spots/add-spot", viewName);
        verify(parkingSpotRepository).save(parkingSpot);  // Verify that the parking spot was saved
        verify(redirectAttributes).addFlashAttribute("successMessage", "Your new Parking Spot is now Added!");
    }
    @Test
    public void testSearchLocations_WithQuery_ShouldReturnMatchingSpots() {
        // Arrange
        String query = "Location1";
        List<ParkingSpot> expectedSpots = List.of(new ParkingSpot(), new ParkingSpot());
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "Available", query, query)).thenReturn(expectedSpots);

        // Act
        List<ParkingSpot> result = parkingSpotController.searchLocations(query);

        // Assert
        assertEquals(expectedSpots.size(), result.size());
        verify(parkingSpotRepository).findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                "Available", query, query);
    }

    @Test
    void testSearchLocations_WithoutQuery_ShouldReturnEmptyList() {
        // Act
        List<ParkingSpot> result = parkingSpotController.searchLocations("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testShowSearchParkingSpotsForm_UserNotLoggedIn_ShouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm("Location1", "Available", model, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testShowSearchParkingSpotsForm_NoLocation_ShouldRedirectWithError() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm("", "Available", model, session, redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Please enter a location to search.");
    }

    @Test
    void testShowSearchParkingSpotsForm_ValidSearchResults_ShouldReturnSearchView() {
        // Arrange
        String location = "Location1";
        when(session.getAttribute("userId")).thenReturn(1L);
        List<ParkingSpot> parkingSpots = List.of(new ParkingSpot());
        when(parkingSpotRepository.findByLocationContainingIgnoreCaseAndAvailabilityIgnoreCase(
                location, "Available")).thenReturn(parkingSpots);

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm(location, "Available", model, session, redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", viewName);
        verify(model).addAttribute("parkingSpots", parkingSpots);
    }

    @Test
    void testShowSearchParkingSpotsForm_SearchWithAvailabilityAll_ShouldReturnSearchView() {
        // Arrange
        String location = "Location1";
        String availability = "all";
        when(session.getAttribute("userId")).thenReturn(1L);
        List<ParkingSpot> parkingSpots = List.of(new ParkingSpot(), new ParkingSpot());
        when(parkingSpotRepository.findByLocationContainingIgnoreCase(location))
                .thenReturn(parkingSpots);

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm(location, availability, model, session, redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", viewName);
        verify(model).addAttribute("parkingSpots", parkingSpots);
    }

    @Test
    void testShowSearchParkingSpotsForm_NoResultsFound_ShouldAddErrorMessage() {
        // Arrange
        String location = "Location1";
        String availability = "Available";
        when(session.getAttribute("userId")).thenReturn(1L);
        when(parkingSpotRepository.findByLocationContainingIgnoreCaseAndAvailabilityIgnoreCase(
                location, availability)).thenReturn(List.of());

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm(location, availability, model, session, redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "No parking spots found for the given location and availability.");
    }

    @Test
    void testShowSearchParkingSpotsForm_ExceptionThrown_ShouldAddErrorMessage() {
        // Arrange
        String location = "Location1";
        String availability = "Available";
        when(session.getAttribute("userId")).thenReturn(1L);
        when(parkingSpotRepository.findByLocationContainingIgnoreCaseAndAvailabilityIgnoreCase(
                location, availability)).thenThrow(new RuntimeException("Database error"));

        // Act
        String viewName = parkingSpotController.showSearchParkingSpotsForm(location, availability, model, session, redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "An error occurred while searching for parking spots.");
    }

    @Test
    void testGetParkingSpots_UserNotLoggedIn_ShouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        String viewName = parkingSpotController.getParkingSpots(model, session);

        // Assert
        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testGetParkingSpots_UserNotSpotOwner_ShouldRedirectToSearch() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("OTHER_TYPE");

        // Act
        String viewName = parkingSpotController.getParkingSpots(model, session);

        // Assert
        assertEquals("redirect:/smart-spots/search", viewName);
    }

    @Test
    void testGetParkingSpots_UserIsSpotOwner_ShouldReturnEditParkingSpotView() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        List<ParkingSpot> parkingSpots = List.of(new ParkingSpot(), new ParkingSpot());
        when(updateParkingSpotService.getParkingSpotsForLoggedInUser(1L)).thenReturn(parkingSpots);
        // Act
        String viewName = parkingSpotController.getParkingSpots(model, session);

        // Assert
        assertEquals("EditParkingSpot", viewName);
        verify(model).addAttribute("parkingSpots", parkingSpots);
    }

    @Test
    public void testRemoveParkingSpot_UserNotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = parkingSpotController.removeParkingSpot(1L, model, session, redirectAttributes);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    public void testRemoveParkingSpot_SuccessfulRemoval_ShouldRedirectWithSuccessMessage() {
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);
        ParkingSpot mockSpot = new ParkingSpot();
        mockSpot.setId(1L);
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(mockSpot));

        String viewName = parkingSpotController.removeParkingSpot(1L, model, session, redirectAttributes);

        assertEquals("redirect:/smart-spots/remove", viewName);
        verify(parkingSpotRepository).delete(mockSpot);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Parking spot removed successfully.");
    }

    @Test
    public void testRemoveParkingSpot_ParkingSpotNotFound_ShouldRedirectWithErrorMessage() {
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.empty());

        String viewName = parkingSpotController.removeParkingSpot(1L, model, session, redirectAttributes);

        assertEquals("redirect:/smart-spots/remove", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Parking spot not found.");
    }
    @Test
    public void testShowRemoveParkingSpot_UserNotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    public void testShowRemoveParkingSpot_UserIsSpotOwner_ShouldReturnRemoveParkingSpotView() {
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        List<ParkingSpot> parkingSpots = List.of(new ParkingSpot(), new ParkingSpot());
        when(updateParkingSpotService.getParkingSpotsForLoggedInUser(loggedInUserId)).thenReturn(parkingSpots);

        String viewName = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("RemoveParkingSpot", viewName);
        verify(model).addAttribute("parkingSpots", parkingSpots);
    }

    @Test
    public void testShowRemoveParkingSpot_NoParkingSpots_ShouldRedirectWithErrorMessage() {
        when(session.getAttribute("userId")).thenReturn(loggedInUserId);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        when(updateParkingSpotService.getParkingSpotsForLoggedInUser(loggedInUserId)).thenReturn(List.of());

        String viewName = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("RemoveParkingSpot", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Parking spot not found.");
    }

    @Test
    public void testUpdateParkingSpot_SuccessfulUpdate_ShouldRedirectWithSuccessMessage() {
        // Arrange
        Long spotId = 1L;
        String availability = "Available";
        Double pricePerHour = 20.0;
        String spotType = "Standard";
        String additionalInstructions = "No instructions";

        when(updateParkingSpotService.updateParkingSpot(spotId, availability, pricePerHour, spotType, additionalInstructions))
                .thenReturn(true);

        // Act
        String viewName = parkingSpotController.updateParkingSpot(
                spotId, availability, pricePerHour, spotType, additionalInstructions, redirectAttributes, model);

        // Assert
        assertEquals("redirect:/smart-spots/update-spot", viewName);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Parking spot updated successfully!");
    }
    @Test
    void testUpdateParkingSpot_Success() {
        when(updateParkingSpotService.updateParkingSpot(1L, "Available", 10.0, "Compact", "Near Entrance"))
                .thenReturn(true);

        String result = parkingSpotController.updateParkingSpot(1L, "Available", 10.0, "Compact", "Near Entrance", redirectAttributes, model);

        assertEquals("redirect:/smart-spots/update-spot", result);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Parking spot updated successfully!");
    }

    @Test
    void testUpdateParkingSpot_Failure() {
        when(updateParkingSpotService.updateParkingSpot(1L, "Unavailable", 5.0, "Large", "Back side"))
                .thenReturn(false);

        String result = parkingSpotController.updateParkingSpot(1L, "Unavailable", 5.0, "Large", "Back side", redirectAttributes, model);

        assertEquals("redirect:/smart-spots/update-spot", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Failed to update the parking spot, Try Again.");
    }

    @Test
    void testUpdateParkingSpot_ExceptionHandling() {
        when(updateParkingSpotService.updateParkingSpot(anyLong(), anyString(), anyDouble(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected Error"));

        String result = parkingSpotController.updateParkingSpot(1L, "Available", 10.0, "Compact", "Near Entrance", redirectAttributes, model);

        assertEquals("redirect:/smart-spots/update-spot", result);
        // Exception is logged, no specific verification needed on the log
    }

    @Test
    void testShowRemoveParkingSpot_UserNotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String result = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void testShowRemoveParkingSpot_UserNotSpotOwner() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("VEHICLE_OWNER");

        String result = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("redirect:/smart-spots/search", result);
    }

    @Test
    void testShowRemoveParkingSpot_NoSpotsFound() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        when(updateParkingSpotService.getParkingSpotsForLoggedInUser(1L)).thenReturn(Collections.emptyList());

        String result = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("RemoveParkingSpot", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Parking spot not found.");
    }

    @Test
    void testShowRemoveParkingSpot_SpotsFound() {
        ParkingSpot parkingSpot = new ParkingSpot();
        User user = new User();
        user.setId(1L);
        parkingSpot.setUser(user);
        parkingSpot.setSpotLocation("new Deli");
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability("Available");
        parkingSpot.setPricePerHour(10.0);

        List<ParkingSpot> mockSpots = List.of(parkingSpot);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");
        when(updateParkingSpotService.getParkingSpotsForLoggedInUser(1L)).thenReturn(mockSpots);

        String result = parkingSpotController.showRemoveParkingSpot(model, session, redirectAttributes);

        assertEquals("RemoveParkingSpot", result);
        verify(model).addAttribute("parkingSpots", mockSpots);
    }

    @Test
    void testSearchLocations_QueryProvided() {
        ParkingSpot parkingSpot = new ParkingSpot();
        User user = new User();
        user.setId(1L);
        parkingSpot.setUser(user);
        parkingSpot.setId(1L);
        parkingSpot.setSpotLocation("new Delhi");  // Ensure the spot location matches the query
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability("Available");
        parkingSpot.setPricePerHour(10.0);

        ParkingSpot parkingSpot2 = new ParkingSpot();
        User user2 = new User();
        user2.setId(2L);
        parkingSpot2.setUser(user2);
        parkingSpot2.setId(2L);
        parkingSpot2.setSpotLocation("new Delhi");
        parkingSpot2.setAdditionalInstructions("Near Exit");
        parkingSpot2.setAvailability("Unavailable");
        parkingSpot2.setPricePerHour(129.0);

        // Arrange
        String query = "new Delhi";
        List<ParkingSpot> mockParkingSpots = Arrays.asList(parkingSpot, parkingSpot2);

        // Mock the repository method
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(query), eq(query)))
                .thenReturn(mockParkingSpots);

        // Act
        List<ParkingSpot> result = parkingSpotController.searchLocations(query);

        // Assert
        assertNotNull(result, "Result should not be null.");
        assertEquals(2, result.size(), "Expected 2 matching parking spots.");
        assertEquals(mockParkingSpots, result, "Expected list of matching parking spots.");
    }

    @Test
    void testSearchLocations_QueryIsNullOrEmpty() {
        // Act
        List<ParkingSpot> resultWithNull = parkingSpotController.searchLocations(null);
        List<ParkingSpot> resultWithEmpty = parkingSpotController.searchLocations("");

        // Assert
        assertTrue(resultWithNull.isEmpty(), "Expected an empty list when query is null.");
        assertTrue(resultWithEmpty.isEmpty(), "Expected an empty list when query is empty.");
    }

    @Test
    void testSearchLocations_NoLocationProvided() {
        // Arrange
        String query = " ";
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(query), eq(query)))
                .thenReturn(new ArrayList<>());

        // Act
        when(session.getAttribute("userId")).thenReturn(1L);
        String result = parkingSpotController.showSearchParkingSpotsForm(" ","available",model,session,redirectAttributes);

        // Assert
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Please enter a location to search.");
        assertEquals("SearchParkingSpot", result, "Expected to return the search page when location is not provided.");
    }
    @Test
    void testSearchLocations_LocationNull() {
        // Arrange
        String query = null;
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(query), eq(query)))
                .thenReturn(new ArrayList<>());

        // Act
        when(session.getAttribute("userId")).thenReturn(1L);
        String result = parkingSpotController.showSearchParkingSpotsForm( query,"available",model,session,redirectAttributes);

        // Assert
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Please enter a location to search.");
        assertEquals("SearchParkingSpot", result, "Expected to return the search page when location is not provided.");
    }

    @Test
    void testSearchLocations_AvailabilityAll() {
        ParkingSpot parkingSpot = new ParkingSpot();
        User user = new User();
        user.setId(1L);
        parkingSpot.setUser(user);
        parkingSpot.setId(1L);
        parkingSpot.setSpotLocation("new Deli");
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability("Available");
        parkingSpot.setPricePerHour(10.0);
        ParkingSpot parkingSpot2 = new ParkingSpot();
        user.setId(2L);
        parkingSpot2.setId(2L);
        parkingSpot.setUser(user);
        parkingSpot.setSpotLocation("new Deli");
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability("Unavailable");
        parkingSpot.setPricePerHour(129.0);
        // Arrange
        String location = "new Delhi";
        String availability = "all";
        List<ParkingSpot> mockParkingSpots = Arrays.asList(
                parkingSpot,parkingSpot2
        );

        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(location), eq(location)))
                .thenReturn(mockParkingSpots);
        // Act
        List<ParkingSpot> result = parkingSpotController.searchLocations(location);

        // Assert
        assertEquals(mockParkingSpots, result, "Expected all parking spots matching the location.");
    }

    @Test
    void testSearchLocations_SpecificAvailability() {
        // Arrange
        String query = "Downtown";
        String availability = "Available";
        ParkingSpot parkingSpot = new ParkingSpot();
        User user = new User();
        user.setId(1L);
        parkingSpot.setUser(user);
        parkingSpot.setSpotLocation("Downtown");
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability("Available");
        parkingSpot.setPricePerHour(10.0);
        List<ParkingSpot> mockParkingSpots = Collections.singletonList(
                parkingSpot
        );

        when(session.getAttribute("userId")).thenReturn(1L);
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(query), eq(query)))
                .thenReturn(mockParkingSpots);

        // Act
        String result = parkingSpotController.showSearchParkingSpotsForm( query,"available",model,session,redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", result, "Expected only parking spots with the specified availability.");
    }
    @Test
    void testSearchLocations_SpecificUnAvailable() {
        // Arrange
        String query = "Downtown";
        String availability = "Unavailable";
        ParkingSpot parkingSpot = new ParkingSpot();
        User user = new User();
        user.setId(1L);
        parkingSpot.setUser(user);
        parkingSpot.setSpotLocation("Downtown");
        parkingSpot.setAdditionalInstructions("Near Entrance");
        parkingSpot.setAvailability(availability);
        parkingSpot.setPricePerHour(10.0);
        List<ParkingSpot> mockParkingSpots = Collections.singletonList(
                parkingSpot
        );

        when(session.getAttribute("userId")).thenReturn(1L);
        when(parkingSpotRepository.findByAvailabilityIgnoreCaseAndSpotLocationContainingIgnoreCaseOrLocationContainingIgnoreCase(
                anyString(), eq(query), eq(query)))
                .thenReturn(mockParkingSpots);

        // Act
        String result = parkingSpotController.showSearchParkingSpotsForm( query,availability,model,session,redirectAttributes);

        // Assert
        assertEquals("SearchParkingSpot", result, "Expected only parking spots with the specified availability.");
    }
}