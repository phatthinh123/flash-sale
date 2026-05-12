package com.flashsale.auth.web;


import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String message,
        String token
) {
    public static AuthResponse registered(UUID userId) {
        return new AuthResponse(userId, "Registration successful. Please verify your OTP.", null);
    }

    public static AuthResponse loggedIn(String token) {
        return new AuthResponse(null, "Login successful", token);
    }

    public static AuthResponse otpVerified() {
        return new AuthResponse(null, "OTP verified successfully. You can now login.", null);
    }

    public static AuthResponse loggedOut() {
        return new AuthResponse(null, "Logged out successfully", null);
    }
}
