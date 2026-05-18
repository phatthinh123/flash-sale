package com.flashsale.flashsale.business.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseOrder(
    UUID id,
    UUID userId,
    UUID flashSaleProductId,
    BigDecimal amount,
    OrderStatus status,
    LocalDateTime purchasedAt) {
  public enum OrderStatus {
    PENDING,
    COMPLETED,
    FAILED
  }

  public static PurchaseOrder createCompleted(UUID userId, UUID productId, BigDecimal amount) {
    return new PurchaseOrder(
        null, userId, productId, amount, OrderStatus.COMPLETED, LocalDateTime.now());
  }
}
