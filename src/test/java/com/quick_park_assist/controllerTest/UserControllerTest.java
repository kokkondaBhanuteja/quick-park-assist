package com.quick_park_assist.controllerTest;

import com.quick_park_assist.controller.UserController;
import com.quick_park_assist.dto.UserProfileDTO;
import com.quick_park_assist.dto.UserRegistrationDTO;
import com.quick_park_assist.entity.User;
import com.quick_park_assist.repository.UserRepository;
import com.quick_park_assist.service.IOTPService;
import com.quick_park_assist.service.IRegistrationOTPService;
import com.quick_park_assist.service.IUserService;
import com.quick_park_assist.serviceImpl.UserServiceImpl;
import com.quick_park_assist.util.PasswordMatchValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static com.quick_park_assist.controller.UserController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private PasswordMatchValidator passwordMatchValidator;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;
    @Mock
    private IOTPService otpService;
    @Mock
    private IRegistrationOTPService registrationOTPService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;


    private UserRegistrationDTO mockUserDTO;
    private User mockUser;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUserDTO = new UserRegistrationDTO();
        mockUserDTO.setEmail("test@example.com");
        mockUser = new User();
        mockUser.setId(111L);
        mockUser.setUserType("SPOT_OWNER");
    }

    @Test
    void testShowChangePasswordForm(){
        String viewName = userController.showChangePasswordForm();
        assertEquals("changePassword",viewName);
    }
    @Test
    void  testShowReactivateForm(){
        String viewName = userController.showReactivationPage();
        assertEquals("reactivate",viewName);
    }

    @Test
    void testShowRegistrationForm() {
        // Act
        String viewName = userController.showRegistrationForm(model);

        // Assert
        assertEquals("registration", viewName);
        verify(model).addAttribute(eq("user"), any(UserRegistrationDTO.class));
    }

    @Test
    void testRegisterUser_ValidationErrors() {
        // Arrange
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration", viewName);
        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void testAuthenticateUser_Success() {
        String email = "test@example.com";
        String password = "Test@123";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setActive(true); // Set active true to avoid reactivation flow

        when(userService.authenticateUser(email, password)).thenReturn(user);

        String viewName = userController.loginUser(email, password, session, mock(RedirectAttributes.class));

        assertEquals("redirect:/dashboard", viewName);
        verify(session).setAttribute("userId", user.getId());
    }

    @Test
    void testAuthenticateUser_Failure() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(userService.authenticateUser(email, password)).thenReturn(null);
         redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String viewName = userController.loginUser(email, password, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login?error", viewName);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Invalid email or password");
    }

    @Test
    void testLogout() {
        // Arrange
         redirectAttributes = mock(RedirectAttributes.class);

        // Act
        String viewName = userController.logout(session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login?logout", viewName);
        verify(session).invalidate();
        verify(redirectAttributes).addFlashAttribute("successMessage", "You have been successfully logged out");
    }

    @Test
    void testDashboard_UserLoggedIn() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userFullName")).thenReturn("Test User");

        // Act
        String viewName = userController.dashboard(session, model);

        // Assert
        assertEquals("dashboard", viewName);
        verify(model).addAttribute("userFullName", "Test User");
    }

    @Test
    void testDashboard_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        String viewName = userController.dashboard(session, model);

        // Assert
        assertEquals("redirect:/login", viewName);
    }

    // UserControllerTest.java (add new test methods)

    @Test
    void testDeactivateAccount_Success() {
        Long userId = 1L;
        String userEmail = "test@example.com";
        when(session.getAttribute("userId")).thenReturn(userId);
        when(session.getAttribute("userEmail")).thenReturn(userEmail);

        String viewName = userController.deactivateAccount(session, mock(RedirectAttributes.class));

        assertEquals("redirect:/login", viewName);
        verify(userService).deactivateAccount(userId);
        verify(otpService).sendAccountStatusEmail(userEmail, true);
        verify(session).invalidate();
    }

    @Test
    void testVerifyReactivation_Success() {
        String email = "test@example.com";
        String otp = "123456";
        when(otpService.verifyReactivationOTP(email, otp)).thenReturn(true);

         redirectAttributes = mock(RedirectAttributes.class);
        String viewName = userController.verifyReactivation(email, otp, session, redirectAttributes);

        assertEquals("redirect:/login", viewName);
        verify(userService).reactivateAccount(email);
        verify(otpService).sendAccountStatusEmail(email, false);
    }

    @Test
    void testVerifyReactivation_InvalidOTP() {
        String email = "test@example.com";
        String otp = "123456";
        when(otpService.verifyReactivationOTP(email, otp)).thenReturn(false);
        when(session.getAttribute("otpAttempts")).thenReturn(0);

         redirectAttributes = mock(RedirectAttributes.class);
        String viewName = userController.verifyReactivation(email, otp, session, redirectAttributes);

        assertEquals("redirect:/profile/reactivate", viewName);
        verify(session).setAttribute(eq("otpAttempts"), eq(1));
    }

    @Test
    void testVerifyReactivation_MaxAttemptsReached() {
        String email = "test@example.com";
        String otp = "123456";
        when(session.getAttribute("otpAttempts")).thenReturn(2);

         redirectAttributes = mock(RedirectAttributes.class);
        String viewName = userController.verifyReactivation(email, otp, session, redirectAttributes);

        assertEquals("redirect:/login", viewName);
        verify(session).removeAttribute("otpAttempts");
    }

    @Test
    void testResendReactivationOTP_Success() {
        String email = "test@example.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(session.getAttribute("lastOtpResendTime")).thenReturn(null);

         redirectAttributes = mock(RedirectAttributes.class);
        String viewName = userController.resendReactivationOTP(email, session, redirectAttributes);

        assertEquals("redirect:/profile/reactivate", viewName);
        verify(otpService).sendReactivationOTP(user);
    }

    @Test
    void testResendReactivationOTP_Cooldown() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(session.getAttribute("lastOtpResendTime"))
                .thenReturn(System.currentTimeMillis() - 15000);

        String viewName = userController.resendReactivationOTP(email, session, mock(RedirectAttributes.class));

        assertEquals("redirect:/profile/reactivate", viewName);
        verify(otpService, never()).sendReactivationOTP(any());
    }

    @Test
    void testRegisterUser_Success() {
        UserRegistrationDTO userDTO = setupValidUserDTO();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(anyString())).thenReturn(false);
        when(userService.isPhoneNumberTaken(anyString())).thenReturn(false);

        String viewName = userController.registerUser(userDTO, bindingResult, session,model);

        assertEquals("registration-verify", viewName);
        verify(model).addAttribute("email", userDTO.getEmail());
        verify(registrationOTPService).sendRegistrationOTP(userDTO.getEmail());
    }

    private UserRegistrationDTO setupValidUserDTO() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setFullName("Test User");
        dto.setEmail("test@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setUserType("VEHICLE_OWNER");
        dto.setPassword("Test@123");
        dto.setConfirmPassword("Test@123");
        dto.setAddress("Test Address");
        return dto;
    }
        // Test Case: home()
        @Test
        void testHome() {
            String viewName = userController.home();
            assertEquals("home", viewName);
        }

        // Test Case: showRegistrationForm(Model model)
        @Test
        void testShowRegistrationForm_EmptyModel() {
            when(model.containsAttribute("user")).thenReturn(false);

            String viewName = userController.showRegistrationForm(model);

            assertEquals("registration", viewName);
            verify(model).addAttribute(eq("user"), any(UserRegistrationDTO.class));
        }

        @Test
        void testShowRegistrationForm_ModelContainsUser() {
            when(model.containsAttribute("user")).thenReturn(true);

            String viewName = userController.showRegistrationForm(model);

            assertEquals("registration", viewName);
            verify(model, never()).addAttribute(eq("user"), any(UserRegistrationDTO.class));
        }


        // Test Case: verifyRegistration(...)
        @Test
         void testVerifyRegistration_SuccessfulVerification() {
            // Mock session attributes
            when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(mockUserDTO);
            when(registrationOTPService.verifyRegistrationOTP("test@example.com", "123456")).thenReturn(true);
            when(userService.registerUser(mockUserDTO)).thenReturn(mockUser);

            // Call the method
            String result = userController.verifyRegistration("test@example.com", "123456", session, model, redirectAttributes);

            // Assertions
            assertEquals("redirect:/login", result);
            verify(session).setAttribute(USER_ID, mockUser.getId());
            verify(session).setAttribute(USER_TYPE, mockUser.getUserType());
            verify(session).removeAttribute(PENDING_REGISTRATION);
            verify(session).removeAttribute(OTP_ATTEMPTS);
        }


    @Test
        void testVerifyRegistration_InvalidOTP()  {
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setEmail("test@example.com");
            when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
            when(registrationOTPService.verifyRegistrationOTP(anyString(), anyString())).thenReturn(false);
            when(session.getAttribute("otpAttempts")).thenReturn(0);

            String viewName = userController.verifyRegistration("test@example.com", "wrongOTP", session, model, redirectAttributes);

            assertEquals("registration-verify", viewName);
            verify(session).setAttribute("otpAttempts", 1);
            verify(model).addAttribute("errorMessage", "Invalid verification code. 2 attempts remaining.");
        }

        // Test Case: resendOTP(...)
        @Test
        void testResendOTP_Success() {
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setEmail("test@example.com");
            when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
            when(session.getAttribute("lastOtpResendTime")).thenReturn(null);

            String viewName = userController.resendOTP(session, model, redirectAttributes);

            assertEquals("registration-verify", viewName);
            verify(registrationOTPService).sendRegistrationOTP(userDTO.getEmail());
            verify(session).setAttribute(eq("lastOtpResendTime"), anyLong());
        }

        @Test
        void testResendOTP_Cooldown() {
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setEmail("test@example.com");
            when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
            when(session.getAttribute("lastOtpResendTime")).thenReturn(System.currentTimeMillis() - 15000);

            String viewName = userController.resendOTP(session, model, redirectAttributes);

            assertEquals("registration-verify", viewName);
            verify(registrationOTPService, never()).sendRegistrationOTP(anyString());
            verify(model).addAttribute(eq("errorMessage"), eq("Please wait before requesting a new code"));
        }

    @Test
     void testVerifyRegistration_SessionExpired(){
        // Mock session attributes
        when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(null);

        // Call the method
        String result = userController.verifyRegistration("test@example.com", "123456", session, model, redirectAttributes);

        // Assertions
        assertEquals(REDIRECT_REGISTER, result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Registration session expired. Please register again.");
    }

    @Test
     void testVerifyRegistration_FirstOTPFails() {
        // Mock session attributes
        when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(mockUserDTO);
        when(session.getAttribute(OTP_ATTEMPTS)).thenReturn(null);
        when(registrationOTPService.verifyRegistrationOTP("test@example.com", "wrong_otp")).thenReturn(false);

        // Call the method
        String result = userController.verifyRegistration("test@example.com", "wrong_otp", session, model, redirectAttributes);

        // Assertions
        assertEquals(REGISTRATION_VERIFY, result);
        verify(session).setAttribute(OTP_ATTEMPTS, 1);
        verify(model).addAttribute(ERROR_MESSAGE, "Invalid verification code. 2 attempts remaining.");
        verify(model).addAttribute(EMAIL, "test@example.com");
    }

    @Test
     void testVerifyRegistration_MaximumAttemptsExceeded() {
        // Mock session attributes
        when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(mockUserDTO);
        when(session.getAttribute(OTP_ATTEMPTS)).thenReturn(2);
        when(registrationOTPService.verifyRegistrationOTP("test@example.com", "wrong_otp")).thenReturn(false);

        // Call the method
        String result = userController.verifyRegistration("test@example.com", "wrong_otp", session, model, redirectAttributes);

        // Assertions
        assertEquals(REDIRECT_REGISTER, result);
        verify(session).removeAttribute(PENDING_REGISTRATION);
        verify(session).removeAttribute(OTP_ATTEMPTS);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Too many incorrect attempts. Please register again.");
    }

    @Test
     void testVerifyRegistration_SecondOTPFails()  {
        // Mock session attributes
        when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(mockUserDTO);
        when(session.getAttribute(OTP_ATTEMPTS)).thenReturn(1);
        when(registrationOTPService.verifyRegistrationOTP("test@example.com", "wrong_otp")).thenReturn(false);

        // Call the method
        String result = userController.verifyRegistration("test@example.com", "wrong_otp", session, model, redirectAttributes);

        // Assertions
        assertEquals(REGISTRATION_VERIFY, result);
        verify(session).setAttribute(OTP_ATTEMPTS, 2);
        verify(model).addAttribute(ERROR_MESSAGE, "Invalid verification code. 1 attempts remaining.");
        verify(model).addAttribute(EMAIL, "test@example.com");
    }
    @Test
    void testUpdateProfile_Success() {
        // Arrange
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName("Updated Name");
        profileDTO.setEmail("updated@example.com");

        when(session.getAttribute("userId")).thenReturn(userId);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String result = userController.updateProfile(profileDTO, bindingResult, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/dashboard", result);
        verify(userService).updateProfile(userId, profileDTO);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Profile updated successfully!");
    }

    @Test
    void testUpdateProfile_ValidationError() {
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName("Updated Name");
        profileDTO.setEmail("updated@example.com");

        when(session.getAttribute("userId")).thenReturn(userId);

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = userController.updateProfile(profileDTO, bindingResult, session, redirectAttributes);

        // Assert
        assertEquals("EditProfile", result);
        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void testResendOTP_NoPendingRegistration() {
        // Arrange
        when(session.getAttribute("pendingRegistration")).thenReturn(null);

        // Act
        String result = userController.resendOTP(session, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/register", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Registration session expired. Please register again.");
    }

    @Test
    void testResendReactivationOTP_NoUserFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = userController.resendReactivationOTP(email, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testResendReactivationOTP_UserAlreadyActive() {
        // Arrange
        String email = "activeuser@example.com";
        User activeUser = new User();
        activeUser.setActive(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));

        // Act
        String result = userController.resendReactivationOTP(email, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(otpService);
    }

    @Test
    void testLogin_UserNotActive() throws Exception {
        // Arrange
        String email = "inactive@example.com";
        String password = "password";
        User inactiveUser = new User();
        inactiveUser.setActive(false);
        when(userService.authenticateUser(email, password)).thenReturn(inactiveUser);

        // Act
        String result = userController.loginUser(email, password, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile/reactivate", result);
        verify(otpService).sendReactivationOTP(inactiveUser);
    }
    @Test
     void testLoginUserException() {
        session = mock(HttpSession.class);
        redirectAttributes = mock(RedirectAttributes.class);

        // Simulate exception thrown by userService.authenticateUser()
        when(userService.authenticateUser(anyString(), anyString())).thenThrow(new RuntimeException("Database connection error"));

        // Call the method
        String result = userController.loginUser("test@example.com", "password", session, redirectAttributes);

        // Assertions
        assertEquals("redirect:/login?error", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "An error occurred during login");

        // Verify logs or additional behaviors if required
    }

    @Test
    void testLogin_NullUser() throws Exception {
        // Arrange
        String email = "unknown@example.com";
        String password = "password";
        when(userService.authenticateUser(email, password)).thenReturn(null);

        // Act
        String result = userController.loginUser(email, password, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login?error", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Invalid email or password");
    }

    @Test
    void testReactivateAccount_Success() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        when(otpService.verifyReactivationOTP(email, otp)).thenReturn(true);

        // Act
        String result = userController.verifyReactivation(email, otp, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(userService).reactivateAccount(email);
        verify(otpService).sendAccountStatusEmail(email, false);
    }

    @Test
    void testReactivateAccount_InvalidOTP() {
        // Arrange
        String email = "test@example.com";
        String otp = "wrong_otp";
        when(otpService.verifyReactivationOTP(email, otp)).thenReturn(false);
        when(session.getAttribute("otpAttempts")).thenReturn(1);

        // Act
        String result = userController.verifyReactivation(email, otp, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile/reactivate", result);
        verify(session).setAttribute("otpAttempts", 2);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Invalid OTP. 1 attempts remaining");
    }

    @Test
    void testDeactivateAccount_NullUserId() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        String result = userController.deactivateAccount(session, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
    }
    @Test
    void testViewProfile_UserNotLoggedIn() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.viewProfile(session, model);

        assertEquals(REDIRECT_LOGIN, result);
        verifyNoInteractions(userService, model);
    }

    @Test
    void testViewProfile_UserNotFound() {
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(null);

        String result = userController.viewProfile(session, model);

        assertEquals(REDIRECT_LOGIN, result);
        verify(userService).getUserById(1L);
        verifyNoInteractions(model);
    }

    @Test
    void testViewProfile_UserFound() {
        User mockUser = new User(); // Assume a valid User object
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        String result = userController.viewProfile(session, model);

        assertEquals("DeleteProfile", result);
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void testShowEditProfileForm_UserNotLoggedIn() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.showEditProfileForm(session, model);

        assertEquals(REDIRECT_LOGIN, result);
        verifyNoInteractions(userService, model);
    }

    @Test
    void testShowEditProfileForm_UserNotFound() {
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(null);

        String result = userController.showEditProfileForm(session, model);

        assertEquals(REDIRECT_LOGIN, result);
    }

    @Test
    void testShowEditProfileForm_UserFound() {
        User mockUser = new User();
        mockUser.setFullName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setAddress("Test Address");

        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        String result = userController.showEditProfileForm(session, model);

        assertEquals("EditProfile", result);
        verify(model).addAttribute(eq("userProfile"), any(UserProfileDTO.class));
        verify(model).addAttribute(eq("userEmail"), eq("test@example.com"));
    }
    @Test
    void testUpdateProfile_UserNotLoggedIn() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.updateProfile(new UserProfileDTO(), null, session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
    }

    @Test
    void testUpdateProfile_HasValidationErrors() {
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        when(session.getAttribute(USER_ID)).thenReturn(1L);

        String response = userController.updateProfile(new UserProfileDTO(), result, session, redirectAttributes);

        assertEquals("EditProfile", response);
    }

    @Test
    void testUpdateProfile_Exception() {
        UserProfileDTO profileDTO = new UserProfileDTO();
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        doThrow(new RuntimeException("Error")).when(userService).updateProfile(anyLong(), any());

        String response = userController.updateProfile(profileDTO, bindingResult, session, redirectAttributes);

        assertEquals("redirect:/profile/edit", response);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Error updating profile: ERROR ");
    }

    @Test
    void testDeactivateAccount_UserNotLoggedIn() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.deactivateAccount(session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
    }
    @Test
    void showLoginPage(){
        String viewName = userController.showLoginPage(model);
        assertEquals("login",viewName);
    }

    @Test
    void testDeactivateAccount_Exception() {
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        doThrow(new RuntimeException("Error")).when(userService).deactivateAccount(anyLong());

        String result = userController.deactivateAccount(session, redirectAttributes);

        assertEquals("redirect:/profileAction", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Error deactivating account: Error");
    }
    @Test
    void testDeleteAccount_UserNotLoggedIn() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.deleteAccount(session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
    }

    @Test
    void testResendOTP_SessionExpired() {
        when(session.getAttribute("pendingRegistration")).thenReturn(null);

        String result = userController.resendOTP(session, model, redirectAttributes);

        assertEquals("redirect:/register", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Registration session expired. Please register again.");
    }
    @Test
    void testResendOTP_CooldownNotOver() {
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setEmail("test@example.com");
        when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
        when(session.getAttribute("lastOtpResendTime")).thenReturn(System.currentTimeMillis() - 15000); // 15 seconds ago

        String result = userController.resendOTP(session, model, redirectAttributes);

        assertEquals("registration-verify", result);
        verify(registrationOTPService, never()).sendRegistrationOTP(anyString());
        verify(model).addAttribute("errorMessage", "Please wait before requesting a new code");
    }
    @Test
    void testResendOTP_ExceptionThrown() {
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setEmail("test@example.com");
        when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
        when(session.getAttribute("lastOtpResendTime")).thenReturn(null);
        doThrow(new RuntimeException("OTP service error")).when(registrationOTPService).sendRegistrationOTP(userDTO.getEmail());

        String result = userController.resendOTP(session, model, redirectAttributes);

        assertEquals("registration-verify", result);
        verify(model).addAttribute("errorMessage", "Failed to send new code. Please try again.");
    }
    @Test
    void testResendOTP_NullPointerException() {
        // Arrange: Simulate `pendingRegistration` being null
        when(session.getAttribute("pendingRegistration")).thenReturn(null);

        // Act
        String result = userController.resendOTP(session, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/register", result); // Should redirect if session expired
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Registration session expired. Please register again.");
    }

    @Test
    void testRegisterUser_PhoneNumberAlreadyTaken() {
        // Arrange
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setPhoneNumber("1234567890");
        userDTO.setEmail("test@example.com"); // Ensure all required fields are set

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(anyString())).thenReturn(false);
        when(userService.isPhoneNumberTaken(anyString())).thenReturn(true);
        when(session.getAttribute(anyString())).thenReturn(null); // Ensure session doesn't throw NullPointerException

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration", result);
        verify(bindingResult).rejectValue("phoneNumber", "phone.exists", "Phone number already registered");
    }

    @Test
    void testShowChangePasswordForm_ReturnsCorrectView() {
        String viewName = userController.showChangePasswordForm();
        assertEquals("changePassword", viewName);
    }

    @Test
    void testShowReactivationPage_ReturnsCorrectView() {
        String viewName = userController.showReactivationPage();
        assertEquals("reactivate", viewName);
    }

    @Test
    void testDeleteAccount_Success() {
        Long userId = 1L;
        when(session.getAttribute(USER_ID)).thenReturn(userId);
        when(session.getAttribute(USER_TYPE)).thenReturn("VEHICLE_OWNER");

        String result = userController.deleteAccount(session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
        verify(userService).deleteAccount(userId, "VEHICLE_OWNER");
        verify(session).invalidate();
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MESSAGE, "Account deleted successfully");
    }

    @Test
    void testDeleteAccount_ExceptionHandling() {
        Long userId = 1L;
        when(session.getAttribute(USER_ID)).thenReturn(userId);
        when(session.getAttribute(USER_TYPE)).thenReturn("VEHICLE_OWNER");

        doThrow(new RuntimeException("Delete error")).when(userService).deleteAccount(userId, "VEHICLE_OWNER");

        String result = userController.deleteAccount(session, redirectAttributes);

        assertEquals("redirect:/profileAction", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Error deleting account: Delete error");
    }

    @Test
    void testVerifyReactivation_MaxAttemptsExceeded() {
        String email = "test@example.com";
        String otp = "123456";

        when(session.getAttribute(OTP_ATTEMPTS)).thenReturn(2);

        String result = userController.verifyReactivation(email, otp, session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
        verify(session).removeAttribute(OTP_ATTEMPTS);
        verify(redirectAttributes).addFlashAttribute(ERROR_MESSAGE, "Too many incorrect attempts. Please try again.");
    }




    @Test
    void testUpdateProfile_PhoneNumberValidation() {
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setPhoneNumber("invalid");

        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = userController.updateProfile(profileDTO, bindingResult, session, redirectAttributes);

        assertEquals("EditProfile", result);
    }

    @Test
    void testViewProfile_SessionExpired() {
        when(session.getAttribute(USER_ID)).thenReturn(null);

        String result = userController.viewProfile(session, model);

        assertEquals(REDIRECT_LOGIN, result);
        verifyNoInteractions(userService, model);
    }


    @Test
    void testRegisterUser_SystemException() {
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setEmail("test@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(anyString())).thenThrow(new RuntimeException("Database Error"));

        String result = userController.registerUser(userDTO, bindingResult, session, model);

        assertEquals(REGISTRATION, result);
        verify(model).addAttribute(eq(ERROR_MESSAGE), contains("An error occurred"));
    }

    @Test
    void testDashboard_InvalidUserType() {
        when(session.getAttribute(USER_ID)).thenReturn(1L);
        when(session.getAttribute(USER_TYPE)).thenReturn(null);
        when(session.getAttribute(USER_FULL_NAME)).thenReturn("Test User");

        String result = userController.dashboard(session, model);

        assertEquals("dashboard", result);
        verify(model).addAttribute(USER_FULL_NAME, "Test User");
    }

    @Test
    void testLoginUser_SessionAttributeVerification() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setUserType("VEHICLE_OWNER");
        user.setActive(true);

        when(userService.authenticateUser(anyString(), anyString())).thenReturn(user);

        String result = userController.loginUser("test@example.com", "password", session, redirectAttributes);

        assertEquals(REDIRECT_DASHBOARD, result);
        verify(session).setAttribute(USER_ID, 1L);
        verify(session).setAttribute(USER_EMAIL, "test@example.com");
        verify(session).setAttribute(USER_FULL_NAME, "Test User");
        verify(session).setAttribute("loggedInUser", user);
        verify(session).setAttribute(USER_TYPE, "VEHICLE_OWNER");
        verify(session).setAttribute("userIsThere", true);
    }

    @Test
    void testResendOTP_ExceptionInOTPService() {
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setEmail("test@example.com");

        when(session.getAttribute(PENDING_REGISTRATION)).thenReturn(userDTO);
        when(session.getAttribute(LAST_OTP_RESEND_TIME)).thenReturn(null);
        doThrow(new RuntimeException("OTP Service Error"))
                .when(registrationOTPService).sendRegistrationOTP(anyString());

        String result = userController.resendOTP(session, model, redirectAttributes);

        assertEquals(REGISTRATION_VERIFY, result);
        verify(model).addAttribute(ERROR_MESSAGE, "Failed to send new code. Please try again.");
        verify(model).addAttribute(EMAIL, "test@example.com");
    }

    @Test
    void testVerifyReactivation_SuccessWithCleanup() {
        String email = "test@example.com";
        String otp = "123456";

        when(otpService.verifyReactivationOTP(email, otp)).thenReturn(true);

        String result = userController.verifyReactivation(email, otp, session, redirectAttributes);

        assertEquals(REDIRECT_LOGIN, result);
        verify(session).removeAttribute(OTP_ATTEMPTS);
        verify(userService).reactivateAccount(email);
        verify(otpService).sendAccountStatusEmail(email, false);
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MESSAGE, "Account reactivated successfully. Please login.");
    }

    @Test
    void testViewProfile_UserNotFoundInService() {
        Long userId = 1L;
        when(session.getAttribute(USER_ID)).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(null);

        String result = userController.viewProfile(session, model);

        assertEquals(REDIRECT_LOGIN, result);
        verify(userService).getUserById(userId);
        verifyNoInteractions(model);
    }

    @Test
    void testUpdateProfile_NullBindingResult() {
        Long userId = 1L;
        UserProfileDTO profileDTO = new UserProfileDTO();
        when(session.getAttribute(USER_ID)).thenReturn(userId);

        String result = userController.updateProfile(profileDTO, null, session, redirectAttributes);

        assertEquals("redirect:/profile/edit", result);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR_MESSAGE), contains("Error updating profile"));
    }
    @Test
    void testRegisterUser_WithInitialValidationErrors() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration", result, "Should return to registration page due to validation errors.");
        verify(bindingResult, times(3)).hasErrors();
    }

    @Test
    void testRegisterUser_WithDuplicateEmail() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(userDTO.getEmail())).thenReturn(true);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration", result, "Should return to registration page due to duplicate email.");
        verify(bindingResult).rejectValue("email", "email.exists", "Email already registered");
    }

    @Test
    void testRegisterUser_WithWeakPassword() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("12345");  // Weak password
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(userDTO.getEmail())).thenReturn(false);
        when(userService.isPhoneNumberTaken(userDTO.getPhoneNumber())).thenReturn(false);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration-verify", result, "Should return to registration page due to weak password.");
        verify(bindingResult).rejectValue("password", "weak Password",
                "Password must be at least 6 characters and contain at least one letter, one number, and one special character");
    }

    @Test
    void testRegisterUser_PasswordMissingNumber() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("Password@");  // Missing a number
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration-verify", result, "Should return to registration page due to weak password.");
        verify(bindingResult).rejectValue("password", "weak Password",
                "Password must be at least 6 characters and contain at least one letter, one number, and one special character");
    }

    @Test
    void testRegisterUser_PasswordMissingSpecialCharacter() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("Password1");  // Missing a special character
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration-verify", result, "Should return to registration page due to weak password.");
        verify(bindingResult).rejectValue("password", "weak Password",
                "Password must be at least 6 characters and contain at least one letter, one number, and one special character");
    }

    @Test
    void testRegisterUser_PasswordTooShort() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("P@1");  // Too short (less than 6 characters)
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertEquals("registration-verify", result, "Should return to registration page due to weak password.");
        verify(bindingResult).rejectValue("password", "weak Password",
                "Password must be at least 6 characters and contain at least one letter, one number, and one special character");
    }
    @Test
    void testRegisterUser_ValidPassword() {
        // Arrange
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("Password@1");  // Valid password
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.isEmailTaken(userDTO.getEmail())).thenReturn(false);
        when(userService.isPhoneNumberTaken(userDTO.getPhoneNumber())).thenReturn(false);

        // Act
        String result = userController.registerUser(userDTO, bindingResult, session, model);

        // Assert
        assertNotEquals("registration", result, "Should proceed with registration since the password is valid.");
        verify(bindingResult, never()).rejectValue("password", "weak Password",
                "Password must be at least 6 characters and contain at least one letter, one number, and one special character");
    }


    @Test
    public void testOtpResendTooSoon() {
        HttpSession session = mock(HttpSession.class);
        UserRegistrationDTO userDTO = createUserDTO();
        userDTO.setPassword("Password@1");
        when(session.getAttribute("pendingRegistration")).thenReturn(userDTO);
        // Mock the session to return a lastResendTime of 5 seconds ago (less than 30 seconds)
        long fiveSecondsAgo = System.currentTimeMillis() - 5000;
        when(session.getAttribute("lastOtpResendTime")).thenReturn(fiveSecondsAgo);

        String result = userController.resendOTP(session,model,redirectAttributes);

        // Assertion: timeElapsed < 30000 should return true
        assertEquals("registration-verify",result, "OtpResendTooSoon should return true for timeElapsed < 30000 milliseconds");
        verify(model).addAttribute("errorMessage","Please wait before requesting a new code");
    }
    private UserRegistrationDTO createUserDTO() {
        UserRegistrationDTO userDTO = new UserRegistrationDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password@123");
        userDTO.setPhoneNumber("1234567890");
        userDTO.setFullName("John Doe");
        return userDTO;
    }

}