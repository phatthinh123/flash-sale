package com.flashsale.flashsale.business.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Domain model for a flash sale product. */
public record FlashSaleProduct(
    UUID id,
    UUID slotId,
    String productName,
    String description,
    BigDecimal originalPrice,
    BigDecimal flashPrice,
    int totalQuantity,
    int remainingQuantity,
    int version) {
  /** Returns true if there is stock available. */
  public boolean isAvailable() {
    return remainingQuantity > 0;
  }

  /** Returns the discount percentage. */
  public int discountPercent() {
    if (originalPrice.compareTo(BigDecimal.ZERO) == 0) return 0;
    return 100
        - flashPrice
            .multiply(BigDecimal.valueOf(100))
            .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
            .intValue();
  }
}
