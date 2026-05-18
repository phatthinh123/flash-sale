package com.flashsale.auth.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OtpToken(
    UUID id,
    UUID userId,
    String otpCode,
    String target,
    TargetType targetType,
    LocalDateTime expiresAt,
    boolean used,
    LocalDateTime createdAt) {
  public enum TargetType {
    EMAIL,
    PHONE
  }

  public boolean isValid(String code) {
    return !used && otpCode.equals(code) && LocalDateTime.now().isBefore(expiresAt);
  }
}
