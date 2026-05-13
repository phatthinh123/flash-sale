package com.flashsale.flashsale.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseResponse(
        UUID orderId,
        UUID productId,
        BigDecimal amount,
        String status,
        LocalDateTime purchasedAt,
        String message
) {
}
