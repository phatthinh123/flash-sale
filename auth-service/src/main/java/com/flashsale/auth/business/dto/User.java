package com.flashsale.auth.business.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record User(
    UUID id,
    String email,
    String phone,
    String passwordHash,
    BigDecimal balance,
    boolean emailVerified,
    boolean phoneVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public boolean isVerified() {
    return (email != null && emailVerified) || (phone != null && phoneVerified);
  }
  //
  //    public User deductBalance(BigDecimal amount) {
  //        if (balance.compareTo(amount) < 0) {
  //            throw new IllegalStateException("Insufficient balance");
  //        }
  //        return new User(id, email, phone, passwordHash, balance.subtract(amount),
  //                emailVerified, phoneVerified, createdAt, LocalDateTime.now());
  //    }
}
