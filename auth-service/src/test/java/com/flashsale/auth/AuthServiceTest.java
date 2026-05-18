package com.flashsale.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flashsale.auth.business.dto.OtpToken;
import com.flashsale.auth.business.dto.User;
import com.flashsale.auth.business.port.AuthPort;
import com.flashsale.auth.business.port.CommunicationPort;
import com.flashsale.auth.business.port.OtpRepository;
import com.flashsale.auth.business.port.UserRepository;
import com.flashsale.auth.service.AuthService;
import com.flashsale.auth.web.port.TokenProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private OtpRepository otpRepository;
  @Mock private CommunicationPort communicationPort;
  @Mock private TokenProvider tokenProvider;
  @Mock private PasswordEncoder passwordEncoder;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService =
        new AuthService(
            userRepository, otpRepository, communicationPort, passwordEncoder, tokenProvider);
  }

  @Test
  void register_withEmail_success() {
    // given
    AuthPort.RegisterCommand command =
        new AuthPort.RegisterCommand("test@example.com", null, "password123");

    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

    User savedUser =
        new User(
            UUID.randomUUID(),
            "test@example.com",
            null,
            "hashedPassword",
            BigDecimal.valueOf(1000.0),
            false,
            false,
            LocalDateTime.now(),
            LocalDateTime.now());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    // when
    User result = authService.register(command);

    // then
    assertNotNull(result);
    assertEquals("test@example.com", result.email());

    verify(userRepository).save(any(User.class));
    verify(otpRepository).save(any(OtpToken.class));

    ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
    verify(communicationPort).sendOTP(eq("test@example.com"), otpCaptor.capture(), eq("EMAIL"));

    String generatedOtp = otpCaptor.getValue();
    assertNotNull(generatedOtp);
    assertEquals(6, generatedOtp.length());
  }

  @Test
  void register_existingEmail_throwsException() {
    // given
    AuthPort.RegisterCommand command =
        new AuthPort.RegisterCommand("test@example.com", null, "password123");
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

    // when then
    assertThrows(IllegalStateException.class, () -> authService.register(command));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void verifyOtp_validCode_success() {
    // given
    UUID userId = UUID.randomUUID();
    String otpCode = "123456";

    OtpToken token =
        new OtpToken(
            UUID.randomUUID(),
            userId,
            otpCode,
            "test@example.com",
            OtpToken.TargetType.EMAIL,
            LocalDateTime.now().plusMinutes(5),
            false,
            LocalDateTime.now());

    when(otpRepository.findLatestUnusedByUserId(userId)).thenReturn(Optional.of(token));

    // when
    authService.verifyOTP(userId, otpCode);

    // then
    verify(otpRepository).markAsUsed(token.id());
    verify(userRepository).updateVerificationStatus(userId, "EMAIL", true);
  }

  @Test
  void verifyOtp_invalidCode_throwsException() {
    // given
    UUID userId = UUID.randomUUID();
    String otpCode = "123456";
    String wrongCode = "654321";

    OtpToken token =
        new OtpToken(
            UUID.randomUUID(),
            userId,
            wrongCode,
            "test@example.com",
            OtpToken.TargetType.EMAIL,
            LocalDateTime.now().plusMinutes(5),
            false,
            LocalDateTime.now());

    when(otpRepository.findLatestUnusedByUserId(userId)).thenReturn(Optional.of(token));

    // when then
    assertThrows(IllegalArgumentException.class, () -> authService.verifyOTP(userId, otpCode));
    verify(otpRepository, never()).markAsUsed(any());
    verify(userRepository, never()).updateVerificationStatus(any(), any(), anyBoolean());
  }

  @Test
  void login_validCredentials_returnsToken() {
    // given
    AuthPort.LoginCommand command = new AuthPort.LoginCommand("test@example.com", "password123");

    User user =
        new User(
            UUID.randomUUID(),
            "test@example.com",
            null,
            "hashedPassword",
            BigDecimal.valueOf(1000.0),
            true,
            false,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
    when(tokenProvider.generateToken(user.id().toString())).thenReturn("jwt-token");

    // when
    String token = authService.login(command);

    // then
    assertEquals("jwt-token", token);
  }

  @Test
  void login_unverifiedUser_throwsException() {
    // Arrange
    AuthPort.LoginCommand command = new AuthPort.LoginCommand("test@example.com", "password123");

    User user =
        new User(
            UUID.randomUUID(),
            "test@example.com",
            null,
            "hashedPassword",
            BigDecimal.valueOf(1000.0),
            false,
            false,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

    // when then
    assertThrows(IllegalStateException.class, () -> authService.login(command));
    verify(tokenProvider, never()).generateToken(any());
  }

  @Test
  void logout_success() {
    // Arrange
    String token = "jwt-token";

    // Act
    authService.logout(token);

    // Assert
    verify(tokenProvider).blacklistToken(token);
  }
}
