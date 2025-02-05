package com.quick_park_assist.controllerTest;


import com.quick_park_assist.controller.VehicleController;
import com.quick_park_assist.dto.VehicleDTO;
import com.quick_park_assist.entity.Vehicle;
import com.quick_park_assist.repository.VehicleRepository;
import com.quick_park_assist.service.IVehicleService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import static com.quick_park_assist.controller.VehicleController.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleControllerTest {

	@Mock
	private IVehicleService vehicleService;

	@Mock
	private VehicleRepository vehicleRepository;

	@Mock
	private HttpSession session;

	@Mock
	private Model model;

	@Mock
	private BindingResult bindingResult;

	@Mock
	private RedirectAttributes redirectAttributes;

	@InjectMocks
	private VehicleController vehicleController;
	private static final String VEHICLE_NUMBER = "ABC1234";
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testListVehicles_UserInSession() {
		Long userId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);
		List<Vehicle> vehicles = new ArrayList<>();
		vehicles.add(new Vehicle());
		when(vehicleService.getVehiclesByUserId(userId)).thenReturn(vehicles);

		String viewName = vehicleController.listVehicles(session, model);
		assertEquals("EditVehicle", viewName);
		verify(model).addAttribute("vehicles", vehicles);
	}

	@Test
	void testAddVehicle_ValidData() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleRepository.existsVehicleByVehicleNumber(anyString())).thenReturn(false);

		String viewName = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
		verify(redirectAttributes).addFlashAttribute("successMessage", "Vehicle added successfully!");
	}

	@Test
	void testAddVehicle_nullUser() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testAddVehicle_DuplicateVehicleNumber() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		vehicleDTO.setVehicleNumber("ABC123");
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleRepository.existsVehicleByVehicleNumber("ABC123")).thenReturn(true);

		String viewName = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("redirect:/vehicles/add", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage",
				"Vehicle Already Registered. Enter new Vehicle Number!");
	}

	@Test
	void testAddVehicle_InvalidData() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(true);

		String viewName = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("AddVehicle", viewName);
	}

	@Test
	void testViewVehicle_NotInSession() {
		when(session.getAttribute("userId")).thenReturn(null);
		String viewName = vehicleController.viewVehicle(1L, session, model, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testViewVehicle_Valid() {
		Long userId = 1L;
		Long vehicleId = 1L;
		Vehicle vehicle = new Vehicle();
		when(session.getAttribute("userId")).thenReturn(userId);
		when(vehicleService.getVehicleByIdAndUserId(vehicleId, userId)).thenReturn(vehicle);

		String viewName = vehicleController.viewVehicle(vehicleId, session, model, redirectAttributes);
		assertEquals("ListVehicle", viewName);
		verify(model).addAttribute("vehicle", vehicle);
	}

	@Test
	void testViewVehicle_NotFound() {
		Long userId = 1L;
		Long vehicleId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);
		when(vehicleService.getVehicleByIdAndUserId(vehicleId, userId))
				.thenThrow(new RuntimeException("Vehicle not found"));

		String viewName = vehicleController.viewVehicle(vehicleId, session, model, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Vehicle not found");
	}

	@Test
	void testDeleteVehicle_Valid() {
		Long userId = 1L;
		Long vehicleId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);

		String viewName = vehicleController.deleteVehicle(vehicleId, session, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
		verify(redirectAttributes).addFlashAttribute("successMessage", "Vehicle deleted successfully!");
	}

	// ---- Tests for listVehicles ----
	@Test
	void testListVehicles_UserNotInSession() {
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.listVehicles(session, model);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testListVehicles_ValidUser_EmptyVehicles() {
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleService.getVehiclesByUserId(1L)).thenReturn(Collections.emptyList());

		String viewName = vehicleController.listVehicles(session, model);
		assertEquals("EditVehicle", viewName);
		verify(model).addAttribute("vehicles", Collections.emptyList());
	}

	@Test
	void testListVehicles_ValidUser_WithVehicles() {
		Long userId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);

		List<Vehicle> vehicles = new ArrayList<>();
		vehicles.add(new Vehicle());
		when(vehicleService.getVehiclesByUserId(userId)).thenReturn(vehicles);

		String viewName = vehicleController.listVehicles(session, model);
		assertEquals("EditVehicle", viewName);
		verify(model).addAttribute("vehicles", vehicles);
	}

	// ---- Tests for addVehicle ----

	@Test
	void testAddVehicle_ExceptionHandling() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleRepository.existsVehicleByVehicleNumber(anyString())).thenReturn(false);
		doThrow(new RuntimeException("Database error")).when(vehicleService).addVehicle(anyLong(),
				any(VehicleDTO.class));

		String viewName = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("AddVehicle", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error adding vehicle: Database error");
	}

	// ---- Tests for viewVehicle ----
	@Test
	void testViewVehicle_UserNotInSession() {
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.viewVehicle(1L, session, model, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testViewVehicle_ValidData() {
		Long userId = 1L;
		Long vehicleId = 1L;
		Vehicle vehicle = new Vehicle();
		when(session.getAttribute("userId")).thenReturn(userId);
		when(vehicleService.getVehicleByIdAndUserId(vehicleId, userId)).thenReturn(vehicle);

		String viewName = vehicleController.viewVehicle(vehicleId, session, model, redirectAttributes);
		assertEquals("ListVehicle", viewName);
		verify(model).addAttribute("vehicle", vehicle);
	}

	// ---- Tests for deleteVehicle ----
	@Test
	void testDeleteVehicle_UserNotInSession() {
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.deleteVehicle(1L, session, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testDeleteVehicle_ExceptionHandling() {
		Long userId = 1L;
		Long vehicleId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);
		doThrow(new RuntimeException("Deletion error")).when(vehicleService).deleteVehicle(vehicleId, userId);

		String viewName = vehicleController.deleteVehicle(vehicleId, session, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error deleting vehicle: Deletion error");
	}

	// ---- Tests for showAddVehicleForm ----
	@Test
	void testShowAddVehicleForm_UserNotInSession() {
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.showAddVehicleForm(session, model);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testShowAddVehicleForm_ValidUser() {
		when(session.getAttribute("userId")).thenReturn(1L);

		String viewName = vehicleController.showAddVehicleForm(session, model);
		assertEquals("AddVehicle", viewName);
		verify(model).addAttribute(eq("vehicle"), any(VehicleDTO.class));
	}

	@Test
	void testSearchVehicle_NullUser() {
		when(session.getAttribute("userId")).thenReturn(null);
		
		String result = vehicleController.searchVehicle("NOT_EXIST", session, model);
		assertEquals("redirect:/login", result);
	}
	
	@Test
	void testSearchVehicle_VehicleNotFound() {
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber("NOT_EXIST")).thenReturn(Optional.empty());

		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(VEHICLE_NUMBER, session, model);
		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("errorMessage", "No vehicle found with this number");
	}
	
	
	@Test
	void testSearchVehicle_VehicelFoundNotEmpty() {
		Vehicle vehicle=new Vehicle();
		when(session.getAttribute("userId")).thenReturn(2L);
		when(vehicleController.isSpotOwner(session)).thenReturn(true);
		when(vehicleRepository.findByVehicleNumber("NOT_EXIST")).thenReturn(Optional.of(vehicle));
		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(VEHICLE_NUMBER, session, model);
		assertEquals("SearchVehicle", result);
	}
	
	@Test
	void testSearchVehicle_RunTimeException() {
		when(session.getAttribute("userId")).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber("NOT_EXIST")).thenThrow((new RuntimeException("Service Error")));
		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle("NOT_EXIST", session, model);
		verify(model).addAttribute("errorMessage", "Error searching vehicle: Service Error");
		assertEquals("SearchVehicle", result);
	}


	@Test
	void testAddVehicle_ExceptionWhileAdding() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(session.getAttribute("userId")).thenReturn(2L);
		when(bindingResult.hasErrors()).thenReturn(false);
		when(vehicleRepository.existsVehicleByVehicleNumber(anyString())).thenReturn(false);
		doThrow(new RuntimeException("Service error")).when(vehicleService).addVehicle(anyLong(),
				any(VehicleDTO.class));

		String result = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("AddVehicle", result);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error adding vehicle: Service error");
	}

	@Test
	void testShowEditForm_nullUser() {
		Long vehicleId = 1L;
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.showEditForm(vehicleId, session, model, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void testShowEditForm_User() {
		Long vehicleId = 1L;
		Long userId = 2L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(2L);
		when(vehicleService.getVehicleByIdAndUserId(vehicleId, userId)).thenReturn(new Vehicle());
		Vehicle vehicle = vehicleService.getVehicleByIdAndUserId(vehicleId, userId);

		String viewName = vehicleController.showEditForm(vehicleId, session, model, redirectAttributes);
		assertEquals("/", viewName);
	}

	@Test
	void testShowEditForm_RunTimeException() {
		Long vehicleId = 1L;
		Long userId = 2L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(session.getAttribute("userId")).thenReturn(2L);
		doThrow(new RuntimeException("service Error")).when(vehicleService).getVehicleByIdAndUserId(vehicleId, userId);

		String viewName = vehicleController.showEditForm(vehicleId, session, model, redirectAttributes);
		assertEquals("redirect:/", viewName);
	}
	
	@Test
	void testUpdateVechile() {
		Long vehicleId = 1L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO,bindingResult, session, model, redirectAttributes);
		assertEquals("redirect:/login", viewName);
	}
	
	@Test
	void testUpdateVechile_bindingResultTrue() {
		Long vehicleId = 1L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(true);
		when(session.getAttribute("userId")).thenReturn(2L);

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO,bindingResult, session, model, redirectAttributes);
		assertEquals("vehicles/edit", viewName);
	}
	
	@Test
	void testUpdateVechile_returndashboard() {
		Long vehicleId = 1L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(2L);

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO,bindingResult, session, model, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
	}
	
	@Test
	void testShowSearchForm_NullUser() {
		when(session.getAttribute("userId")).thenReturn(null);

		String viewName = vehicleController.showSearchForm(session, model);
		assertEquals("redirect:/login", viewName);
	}
	
	@Test
	void testShowSearchForm_User() {
		when(session.getAttribute("userId")).thenReturn(2L);
		// Spy on vehicleController to override isSpotOwner()
		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.showSearchForm(session, model);
		assertEquals("SearchVehicle", result);
	}
	@Test
	void testSearchVehicle_ValidVehicle() {
		// Arrange
		Vehicle vehicle = new Vehicle();
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber(VEHICLE_NUMBER)).thenReturn(Optional.of(vehicle));

		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(VEHICLE_NUMBER, session, model);

		// Assert
		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("vehicle", vehicle);
	}

	@Test
	void testSearchVehicle_InvalidVehicle() {
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber(VEHICLE_NUMBER)).thenReturn(Optional.empty());
		when(vehicleController.isSpotOwner(session)).thenReturn(false);

		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(VEHICLE_NUMBER, session, model);


		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("errorMessage", "No vehicle found with this number");
	}

	@Test
	void testSearchVehicle_UserNotLoggedIn() {
		when(session.getAttribute(USER_ID)).thenReturn(null);

		String result = vehicleController.searchVehicle(VEHICLE_NUMBER, session, model);

		assertEquals("redirect:/login", result);
	}

	@Test
	void testSearchVehicle_UserIsSpotOwner() {
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		when(vehicleController.isSpotOwner(session)).thenReturn(true);

		String result = vehicleController.searchVehicle(VEHICLE_NUMBER, session, model);

		assertEquals("redirect:/dashboard", result);
	}

	@Test
	void testSearchVehicle_DatabaseError() {
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber(VEHICLE_NUMBER)).thenThrow(new RuntimeException("DB Error"));
		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(VEHICLE_NUMBER, session, model);

		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("errorMessage", "Error searching vehicle: DB Error");
	}

	@Test
	void testSearchVehicle_NullVehicleNumber() {
		when(session.getAttribute(USER_ID)).thenReturn(1L);

		// Spy on vehicleController to override isSpotOwner()
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle(null, session, model);

		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("errorMessage", "No vehicle found with this number");
	}
	@Test
	void testAddVehicle_MaxVehiclesReached() {
		VehicleDTO vehicleDTO = new VehicleDTO();
		Long userId = 1L;
		when(session.getAttribute("userId")).thenReturn(userId);
		when(bindingResult.hasErrors()).thenReturn(false);
		when(vehicleRepository.existsVehicleByVehicleNumber(anyString())).thenReturn(false);
		doThrow(new RuntimeException("Maximum vehicles limit reached")).when(vehicleService).addVehicle(anyLong(), any(VehicleDTO.class));

		String result = vehicleController.addVehicle(vehicleDTO, bindingResult, session, redirectAttributes);
		assertEquals("AddVehicle", result);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error adding vehicle: Maximum vehicles limit reached");
	}

	@Test
	void testUpdateVehicle_InvalidVehicleData() {
		Long vehicleId = 1L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		vehicleDTO.setVehicleNumber(""); // Invalid vehicle number

		when(session.getAttribute("userId")).thenReturn(2L);
		when(bindingResult.hasErrors()).thenReturn(true);

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO, bindingResult, session, model, redirectAttributes);
		assertEquals("vehicles/edit", viewName);
	}

	@Test
	void testUpdateVehicle_ServiceLayerError() {
		Long vehicleId = 1L;
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(session.getAttribute("userId")).thenReturn(2L);
		when(bindingResult.hasErrors()).thenReturn(false);
		doThrow(new RuntimeException("Update failed")).when(vehicleService).updateVehicle(anyLong(), anyLong(), any(VehicleDTO.class));

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO, bindingResult, session, model, redirectAttributes);
		assertEquals("redirect:/editVehicle", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error updating vehicle: Update failed");
	}

	@Test
	void testDeleteVehicle_VehicleNotFound() {
		Long vehicleId = 1L;
		Long userId = 2L;
		when(session.getAttribute("userId")).thenReturn(userId);
		doThrow(new RuntimeException("Vehicle not found")).when(vehicleService).deleteVehicle(vehicleId, userId);

		String viewName = vehicleController.deleteVehicle(vehicleId, session, redirectAttributes);
		assertEquals("redirect:/dashboard", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error deleting vehicle: Vehicle not found");
	}

	@Test
	void testSearchVehicle_SpotOwnerAttemptSearch() {
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(true).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle("ABC123", session, model);
		assertEquals("redirect:/dashboard", result);
	}

	@Test
	void testShowEditForm_VehicleNotOwnedByUser() {
		Long vehicleId = 1L;
		Long userId = 2L;
		when(session.getAttribute("userId")).thenReturn(userId);
		doThrow(new RuntimeException("Vehicle not found")).when(vehicleService).getVehicleByIdAndUserId(vehicleId, userId);

		String viewName = vehicleController.showEditForm(vehicleId, session, model, redirectAttributes);
		assertEquals("redirect:/", viewName);
	}



	@Test
	void testSearchVehicle_CaseInsensitiveSearch() {
		Vehicle vehicle = new Vehicle();
		vehicle.setVehicleNumber("ABC123");
		when(session.getAttribute(USER_ID)).thenReturn(1L);
		when(vehicleRepository.findByVehicleNumber("abc123")).thenReturn(Optional.of(vehicle));

		VehicleController spyController = Mockito.spy(vehicleController);
		doReturn(false).when(spyController).isSpotOwner(session);

		String result = spyController.searchVehicle("abc123", session, model);
		assertEquals("SearchVehicle", result);
		verify(model).addAttribute("vehicle", vehicle);
	}

	@Test
	void testUpdateVehicle_NonExistentVehicleId() {
		Long vehicleId = 999L; // Non-existent ID
		VehicleDTO vehicleDTO = new VehicleDTO();
		when(bindingResult.hasErrors()).thenReturn(false);
		when(session.getAttribute("userId")).thenReturn(2L);
		doThrow(new RuntimeException("Vehicle not found")).when(vehicleService).updateVehicle(vehicleId, 2L, vehicleDTO);

		String viewName = vehicleController.updateVehicle(vehicleId, vehicleDTO, bindingResult, session, model, redirectAttributes);
		assertEquals("redirect:/editVehicle", viewName);
		verify(redirectAttributes).addFlashAttribute("errorMessage", "Error updating vehicle: Vehicle not found");
	}
	@Test
	void testIsSpotOwner_WhenUserIsSpotOwner() {
		// Arrange
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");

		// Act
		boolean result = vehicleController.isSpotOwner(session);

		// Assert
		assertFalse(result, "Expected false when userType is SPOT_OWNER");
	}

	@Test
	void testIsSpotOwner_WhenUserIsNotSpotOwner() {
		// Arrange
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute("userType")).thenReturn("CUSTOMER");

		// Act
		boolean result = vehicleController.isSpotOwner(session);

		// Assert
		assertTrue(result, "Expected true when userType is not SPOT_OWNER");
	}

	@Test
	void testIsSpotOwner_WhenUserTypeIsNull() {
		// Arrange
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute("userType")).thenReturn(null);

		// Act
		boolean result = vehicleController.isSpotOwner(session);

		// Assert
		assertTrue(result, "Expected true when userType is null");
	}
	@Test
	void testShowSearchForm_UserNotLoggedIn() {
		// Arrange
		when(session.getAttribute("userId")).thenReturn(null);

		// Act
		String result = vehicleController.showSearchForm(session, model);

		// Assert
		assertEquals("redirect:/login", result, "Expected redirect to login if user is not logged in.");
	}

	@Test
	void testShowSearchForm_UserIsSpotOwner() {
		// Arrange
		when(session.getAttribute("userId")).thenReturn(1L);
		when(session.getAttribute("userType")).thenReturn("SPOT_OWNER");

		// Act
		String result = vehicleController.showSearchForm(session, model);

		// Assert
		assertEquals("SearchVehicle", result, "Expected redirect to dashboard for spot owners.");
	}

	@Test
	void testShowSearchForm_UserIsNotSpotOwner() {
		// Arrange
		when(session.getAttribute("userId")).thenReturn(1L);
		when(session.getAttribute("userType")).thenReturn("CUSTOMER");

		// Act
		String result = vehicleController.showSearchForm(session, model);

		// Assert
		assertEquals("redirect:/dashboard", result, "Expected to return the search form view for regular users.");
	}
}
