package com.quick_park_assist.serviceImpl;

import com.quick_park_assist.dto.UserProfileDTO;
import com.quick_park_assist.dto.UserRegistrationDTO;
import com.quick_park_assist.entity.User;
import com.quick_park_assist.repository.*;
import com.quick_park_assist.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    public static final String USER_NOT_FOUND = "User not found";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private AddonRepository addonRepository;
    @Autowired
    private BookingSpotRepository bookingSpotRepository;
    @Autowired
    private ParkingSpotRepository parkingSpotRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User registerUser(UserRegistrationDTO dto) throws PasswordHashingException {
        User user = new User();
        // Set all fields explicitly
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhoneNumber(dto.getPhoneNumber().trim());
        user.setUserType(dto.getUserType());
        user.setPassword(dto.getPassword());
        user.setAddress(dto.getAddress().trim());
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        // Log the user object before saving (for debugging)
        log.info("Attempting to save user: {}", user);

        return userRepository.save(user);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isPhoneNumberTaken(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public User authenticateUser(String email, String password) throws PasswordHashingException {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedInputPassword = hashPassword(password);

            if (password.equals(user.getPassword()) || hashedInputPassword.equals(user.getPassword()) ) {
                return user;
            }
        }
        return null;
    }
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }



    @Override
    public void updateProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        user.setFullName(profileDTO.getFullName().trim());
        user.setEmail(profileDTO.getEmail().trim().toLowerCase());
        user.setPhoneNumber(profileDTO.getPhoneNumber().trim());
        user.setAddress(profileDTO.getAddress().trim());

        userRepository.save(user);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId, String userType) {
        if ("SPOT_OWNER".equals(userType)) {
            parkingSpotRepository.deleteAllByUserId(userId);
            serviceRepository.deleteAllByUserId(userId);
        } else if ("VEHICLE_OWNER".equals(userType)) {
            bookingSpotRepository.deleteAllByUserId(userId);
            reservationRepository.deleteAllByUserId(userId);
            addonRepository.deleteAllByUserId(userId);
            vehicleRepository.deleteAllByUserId(userId);
        }
        // Finally, delete user after all related records are removed
        userRepository.deleteById(userId);
    }
    /**
     * Hashes the provided password using SHA-256 and returns the hashed value.
     *
     * @param password The password to be hashed
     * @return The hashed password as a Base64-encoded string
     * @throws PasswordHashingException if hashing fails
     */
    public String hashPassword(String password) throws PasswordHashingException {
        try {
            // Use "INVALID_ALGO" for testing to force NoSuchAlgorithmException
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordHashingException("Error hashing password", e);
        }
    }


    /**
     * Custom exception for password hashing errors.
     */
    public static class PasswordHashingException extends Exception {
        public PasswordHashingException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    @Override
    public void reactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(User::isActive)
                .orElse(false);
    }


}