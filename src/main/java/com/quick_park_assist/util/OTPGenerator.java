package com.quick_park_assist.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    private static final Random random = new Random();
    public String generateOTP() {
        return String.valueOf( (random.nextInt(900000)) + 100000); // Generates a 6-digit OTP
    }
}
