package com.flashsale.auth.web;

import com.flashsale.auth.business.dto.User;
import com.flashsale.auth.business.port.AuthPort;
import com.flashsale.auth.web.dto.LoginRequest;
import com.flashsale.auth.web.dto.OtpVerifyRequest;
import com.flashsale.auth.web.dto.RegisterRequest;
import com.flashsale.auth.web.mapper.AuthMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "User registration, login, OTP verification, and logout")
public class AuthController {

  private final AuthPort authPort;
  private final AuthMapper authMapper;

  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description = "Register with email or phone number. An OTP will be sent for verification.")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    User user = authPort.register(authMapper.to(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.registered(user.id()));
  }

  @PostMapping("/verify-otp")
  @Operation(summary = "Verify OTP code", description = "Verify the OTP sent during registration")
  public ResponseEntity<AuthResponse> verifyOTP(@Valid @RequestBody OtpVerifyRequest request) {
    authPort.verifyOTP(request.userId(), request.otpCode());
    return ResponseEntity.ok(AuthResponse.otpVerified());
  }

  @PostMapping("/login")
  @Operation(
      summary = "Login",
      description = "Login with email/phone and password. Returns JWT token.")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    String token = authPort.login(authMapper.to(request));
    return ResponseEntity.ok(AuthResponse.loggedIn(token));
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout", description = "Blacklist the current JWT token")
  public ResponseEntity<AuthResponse> logout(Authentication authentication) {
    String token = (String) authentication.getCredentials();
    authPort.logout(token);
    return ResponseEntity.ok(AuthResponse.loggedOut());
  }
}
