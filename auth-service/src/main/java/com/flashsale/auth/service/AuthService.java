package com.flashsale.auth.service;

import com.flashsale.auth.business.dto.OtpToken;
import com.flashsale.auth.business.dto.User;
import com.flashsale.auth.business.port.AuthPort;
import com.flashsale.auth.business.port.CommunicationPort;
import com.flashsale.auth.business.port.OtpRepository;
import com.flashsale.auth.business.port.UserRepository;
import com.flashsale.auth.web.port.TokenProvider;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService implements AuthPort {

  private final UserRepository userRepository;
  private final OtpRepository otpRepository;
  private final CommunicationPort communicationPort;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  @Transactional
  public User register(RegisterCommand command) {
    String email = normalizeToNull(command.email());
    String phone = normalizeToNull(command.phone());

    // Validate that at least email or phone is provided
    if (email == null && phone == null) {
      throw new IllegalArgumentException("Either email or phone must be provided");
    }

    // Check for existing users
    if (email != null && userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered");
    }

    if (phone != null && userRepository.existsByPhone(phone)) {
      throw new IllegalStateException("Phone number already registered");
    }

    User user =
        new User(
            null,
            email,
            phone,
            passwordEncoder.encode(command.password()),
            BigDecimal.valueOf(1000.00), // Default starting balance
            false,
            false,
            LocalDateTime.now(),
            LocalDateTime.now());
    User savedUser = userRepository.save(user);

    // Generate and send OTP
    String target = email != null ? email : phone;
    String targetType = email != null ? "EMAIL" : "PHONE";
    String otpCode = generateOtp();

    OtpToken otpToken =
        new OtpToken(
            null,
            savedUser.id(),
            otpCode,
            target,
            email != null ? OtpToken.TargetType.EMAIL : OtpToken.TargetType.PHONE,
            LocalDateTime.now().plusMinutes(5),
            false,
            LocalDateTime.now());
    otpRepository.save(otpToken);
    communicationPort.sendOTP(target, otpCode, targetType);

    return savedUser;
  }

  @Override
  @Transactional
  public void verifyOTP(UUID userId, String otp) {
    OtpToken otpToken =
        otpRepository
            .findLatestUnusedByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("OTP token not found for user"));

    if (!otpToken.isValid(otp)) {
      throw new IllegalArgumentException("Invalid OTP or expired Token");
    }

    otpRepository.markAsUsed(otpToken.id());
    userRepository.updateVerificationStatus(userId, otpToken.targetType().name(), true);
  }

  @Override
  public String login(LoginCommand command) {
    User user = findUserByIdentifier(command.identifier());
    if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
      throw new IllegalStateException("Invalid password");
    }
    if (!user.isVerified()) {
      throw new IllegalStateException(
          "Acccount is not verified; Please verify email or phone first");
    }

    return tokenProvider.generateToken(user.id().toString());
  }

  @Override
  public void logout(String token) {
    tokenProvider.blacklistToken(token);
  }

  private User findUserByIdentifier(String identifier) {
    // fallback with phone
    return userRepository
        .findByEmail(identifier)
        .or(() -> userRepository.findByPhone(identifier))
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
  }

  private String generateOtp() {
    int otp = 100000 + secureRandom.nextInt(900000);
    return String.valueOf(otp);
  }

  private String normalizeToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
