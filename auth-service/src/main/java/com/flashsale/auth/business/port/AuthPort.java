package com.flashsale.auth.business.port;

import com.flashsale.auth.business.dto.User;

import java.util.UUID;

public interface AuthPort {

    User register(RegisterCommand command);

    void verifyOTP(UUID userId, String otp);

    String login(LoginCommand command);

    void logout(String token);

    record RegisterCommand(String email, String phone, String password) {
    }

    record LoginCommand(String identifier, String password) {
    }
}
