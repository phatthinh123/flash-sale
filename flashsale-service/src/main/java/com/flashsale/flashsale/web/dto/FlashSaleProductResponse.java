package com.flashsale.flashsale.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FlashSaleProductResponse(
        UUID id,
        String productName,
        String description,
        BigDecimal originalPrice,
        BigDecimal flashPrice,
        int discountPercent,
        int totalQuantity,
        int remainingQuantity,
        boolean available
) {
}
