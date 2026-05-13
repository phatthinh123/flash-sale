package com.flashsale.flashsale.business.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String phone,
        BigDecimal balance,
        boolean emailVerified,
        boolean phoneVerified
) {
    public boolean isVerified() {
        return (email != null && emailVerified) || (phone != null && phoneVerified);
    }
}
