package com.quick_park_assist.serviceImplTest;


import com.quick_park_assist.dto.UserProfileDTO;
import com.quick_park_assist.dto.UserRegistrationDTO;
import com.quick_park_assist.entity.User;
import com.quick_park_assist.repository.AddonRepository;
import com.quick_park_assist.repository.BookingSpotRepository;
import com.quick_park_assist.repository.ParkingSpotRepository;
import com.quick_park_assist.repository.ReservationRepository;
import com.quick_park_assist.repository.ServiceRepository;
import com.quick_park_assist.repository.UserRepository;
import com.quick_park_assist.repository.VehicleRepository;
import com.quick_park_assist.service.IOTPService;
import com.quick_park_assist.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private AddonRepository addonRepository;
    @Mock
    private BookingSpotRepository bookingSpotRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Autowired
    private IOTPService otpService;
    private User activeUser;
    private User inactiveUser;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser(){
        // Arrange
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setUserType("Customer");
        dto.setPassword("password123");
        dto.setAddress("123 Main St");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(dto.getFullName());
        savedUser.setEmail(dto.getEmail().toLowerCase());
        savedUser.setPhoneNumber(dto.getPhoneNumber());
        savedUser.setUserType(dto.getUserType());
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testIsEmailTaken() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.isEmailTaken(email);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testIsPhoneNumberTaken() {
        // Arrange
        String phoneNumber = "1234567890";
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // Act
        boolean result = userService.isPhoneNumberTaken(phoneNumber);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByPhoneNumber(phoneNumber);
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testUpdateProfile() {
        // Arrange
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName("John Doe");
        profileDTO.setEmail("john.doe@example.com");
        profileDTO.setPhoneNumber("1234567890");
        profileDTO.setAddress("123 Main St");

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.updateProfile(userId, profileDTO);

        // Assert
        assertEquals(profileDTO.getFullName(), user.getFullName());
        assertEquals(profileDTO.getEmail(), user.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeactivateAccount() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deactivateAccount(userId);

        // Assert
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteAccount() {
        // Arrange
        Long userId = 1L;

        // Act
        userService.deleteAccount(userId,anyString());

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
    // Test for isAccountActive method
    @Test
    void isAccountActive_userIsActive() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));

        boolean result = userService.isAccountActive("active@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).findByEmail("active@example.com");
    }

    @Test
    void isAccountActive_userIsInactive() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        boolean result = userService.isAccountActive("inactive@example.com");

        assertFalse(result);
        verify(userRepository, times(1)).findByEmail("inactive@example.com");
    }

    @Test
    void isAccountActive_userDoesNotExist() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        boolean result = userService.isAccountActive("unknown@example.com");

        assertFalse(result);
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }

    // Test for reactivateAccount method
    @Test
    void reactivateAccount_userReactivatedSuccessfully() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        userService.reactivateAccount("inactive@example.com");

        assertTrue(inactiveUser.isActive()); // User should now be active
        verify(userRepository, times(1)).save(inactiveUser);
    }

    @Test
    void reactivateAccount_userAlreadyActive() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));

        userService.reactivateAccount("active@example.com");

        assertTrue(activeUser.isActive()); // User remains active
        verify(userRepository, times(1)).save(activeUser);
    }

    @Test
    void reactivateAccount_userDoesNotExist() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");
        activeUser.setActive(true);

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.reactivateAccount("unknown@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
        verify(userRepository, times(0)).save(any(User.class)); // Ensure no save attempt was made
    }

    // Add these test cases to UserServiceImplTest.java

    @Test
    void testDeleteAccount_SpotOwner() {
        // Arrange
        Long userId = 1L;
        String userType = "SPOT_OWNER";

        // Act
        userService.deleteAccount(userId, userType);

        // Assert
        verify(parkingSpotRepository).deleteAllByUserId(userId);
        verify(serviceRepository).deleteAllByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteAccount_VehicleOwner() {
        // Arrange
        Long userId = 1L;
        String userType = "VEHICLE_OWNER";

        // Act
        userService.deleteAccount(userId, userType);

        // Assert
        verify(bookingSpotRepository).deleteAllByUserId(userId);
        verify(reservationRepository).deleteAllByUserId(userId);
        verify(addonRepository).deleteAllByUserId(userId);
        verify(vehicleRepository).deleteAllByUserId(userId);
        verify(userRepository).deleteById(userId);
    }



    @Test
    void testUpdateProfile_UserNotFound() {
        // Arrange
        Long userId = 999L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.updateProfile(userId, profileDTO));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeactivateAccount_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.deactivateAccount(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testAuthenticateUser_WithHashedPassword(){
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = userService.authenticateUser(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void testAuthenticateUser_WithPlainPassword() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        User user = new User();
        user.setEmail(email);
        user.setPassword(password); // Store plain password

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = userService.authenticateUser(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void testRegisterUser_WithSpecialCharactersInInputs() {
        // Arrange
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFullName("O'Connor-Smith Jr.");
        dto.setEmail(" user+test@example.com ");
        dto.setPhoneNumber(" +1-234-567-890 ");
        dto.setUserType("Customer");
        dto.setPassword("p@ssw0rd!");
        dto.setAddress(" 123/A, Main St. #42 ");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("O'Connor-Smith Jr.");
        savedUser.setEmail("user+test@example.com");
        savedUser.setPhoneNumber("+1-234-567-890");
        savedUser.setAddress("123/A, Main St. #42");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals("user+test@example.com", result.getEmail());
        assertEquals("+1-234-567-890", result.getPhoneNumber());
        assertEquals("123/A, Main St. #42", result.getAddress());
    }




    @Test
    void testUpdateProfile_WithWhitespaceInFields() {
        // Arrange
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName("   John   Doe   ");
        profileDTO.setEmail("  john.doe@example.com  ");
        profileDTO.setPhoneNumber("  123-456-7890  ");
        profileDTO.setAddress("  42   Main   St  ");

        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateProfile(userId, profileDTO);

        // Assert
        assertEquals("John   Doe", existingUser.getFullName().trim());
        assertEquals("john.doe@example.com", existingUser.getEmail());
        assertEquals("123-456-7890", existingUser.getPhoneNumber());
        assertEquals("42   Main   St", existingUser.getAddress().trim());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testDeleteAccount_WithInvalidUserType() {
        // Arrange
        Long userId = 1L;
        String invalidUserType = "INVALID_TYPE";

        // Act
        userService.deleteAccount(userId, invalidUserType);

        // Assert
        verify(userRepository).deleteById(userId);
        verify(parkingSpotRepository, never()).deleteAllByUserId(userId);
        verify(serviceRepository, never()).deleteAllByUserId(userId);
        verify(bookingSpotRepository, never()).deleteAllByUserId(userId);
        verify(reservationRepository, never()).deleteAllByUserId(userId);
    }


    @Test
    void testAuthenticateUser_WithNonexistentEmail() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        User result = userService.authenticateUser(email, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testReactivateAccount_AlreadyActiveUser() {
        // Arrange
        String email = "active@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        userService.reactivateAccount(email);

        // Assert
        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }



    @Test
    void testIsPhoneNumberTaken_WithFormatting() {
        // Arrange
        String phoneNumber = "+1-234-567-8900";
        when(userRepository.existsByPhoneNumber(phoneNumber.trim())).thenReturn(true);

        // Act
        boolean result = userService.isPhoneNumberTaken(phoneNumber);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByPhoneNumber(phoneNumber.trim());
    }

    @Test
    void testUpdateProfile_WithUnchangedFields() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFullName("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPhoneNumber("1234567890");
        existingUser.setAddress("123 Main St");

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(existingUser.getFullName());
        profileDTO.setEmail(existingUser.getEmail());
        profileDTO.setPhoneNumber(existingUser.getPhoneNumber());
        profileDTO.setAddress(existingUser.getAddress());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateProfile(userId, profileDTO);

        // Assert
        verify(userRepository).save(existingUser);
        assertEquals(existingUser.getFullName(), profileDTO.getFullName());
        assertEquals(existingUser.getEmail(), profileDTO.getEmail());
    }

    @Test
    void testDeactivateAccount_AlreadyInactiveUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deactivateAccount(userId);

        // Assert
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }


    @Test
    void testRegisterUser_WithMaximumLengthValues() {
        // Arrange
        String longString = "a".repeat(255); // Max length typically allowed in DB
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFullName(longString);
        dto.setEmail("user@" + longString + ".com");
        dto.setPhoneNumber("1".repeat(20));
        dto.setUserType("Customer");
        dto.setPassword(longString);
        dto.setAddress(longString);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(longString);
        savedUser.setEmail(("user@" + longString + ".com").toLowerCase());
        savedUser.setPhoneNumber("1".repeat(20));
        savedUser.setAddress(longString);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getEmail(), result.getEmail());
        assertEquals(savedUser.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(savedUser.getAddress(), result.getAddress());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void testUpdateProfile_WithInternationalCharacters() {
        // Arrange
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName("José María García");
        profileDTO.setEmail("jose@example.com");
        profileDTO.setPhoneNumber("+34 666 777 888");
        profileDTO.setAddress("Calle Mayor 123, España");

        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateProfile(userId, profileDTO);

        // Assert
        assertEquals("José María García", existingUser.getFullName());
        assertEquals("jose@example.com", existingUser.getEmail());
        assertEquals("+34 666 777 888", existingUser.getPhoneNumber());
        assertEquals("Calle Mayor 123, España", existingUser.getAddress());
        verify(userRepository).save(existingUser);
    }



    @Test
    void testAuthenticateUser_WithPasswordVersions(){
        // Arrange
        String email = "test@example.com";
        String oldPassword = "oldpassword123";
        String newPassword = "newpassword123";

        User user = new User();
        user.setEmail(email);
        user.setPassword(oldPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        // Test with old password
        User resultOld = userService.authenticateUser(email, oldPassword);
        assertNotNull(resultOld);

        // Update password
        user.setPassword(newPassword);

        // Test with new password
        User resultNew = userService.authenticateUser(email, newPassword);
        assertNotNull(resultNew);

        // Test with old password (should fail)
        User resultFail = userService.authenticateUser(email, oldPassword);
        assertNull(resultFail);
    }

    @Test
    void testCompleteUserLifecycle(){
        // Arrange
        String email = "lifecycle@test.com";
        String password = "password123";

        // Test registration
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setFullName("Lifecycle Test");
        registrationDTO.setEmail(email);
        registrationDTO.setPhoneNumber("1234567890");
        registrationDTO.setUserType("Customer");
        registrationDTO.setPassword(password);
        registrationDTO.setAddress("123 Test St");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(email);
        savedUser.setPassword(password);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        // Act & Assert
        // 1. Register
        User registeredUser = userService.registerUser(registrationDTO);
        assertNotNull(registeredUser);
        assertTrue(registeredUser.isActive());

        // 2. Authenticate
        User authenticatedUser = userService.authenticateUser(email, password);
        assertNotNull(authenticatedUser);

        // 3. Update profile
        UserProfileDTO updateDTO = new UserProfileDTO();
        updateDTO.setFullName("Updated Name");
        updateDTO.setEmail(email);
        updateDTO.setPhoneNumber("0987654321");
        updateDTO.setAddress("456 New St");

        userService.updateProfile(1L, updateDTO);
        assertEquals("Updated Name", savedUser.getFullName());

        // 4. Deactivate
        userService.deactivateAccount(1L);
        assertFalse(savedUser.isActive());

        // 5. Reactivate
        userService.reactivateAccount(email);
        assertTrue(savedUser.isActive());

        // 6. Delete
        userService.deleteAccount(1L, "Customer");
        verify(userRepository).deleteById(1L);
    }

}

