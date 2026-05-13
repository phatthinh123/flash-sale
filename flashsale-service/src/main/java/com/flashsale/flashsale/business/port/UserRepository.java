package com.flashsale.flashsale.business.port;

import com.flashsale.flashsale.business.dto.User;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID userId);

    /**
     * Debits the user's balance atomically (pessimistic lock).
     *
     * @return true if the debit succeeded; false if the user was not found
     * or had insufficient balance.
     */
    boolean debitBalance(UUID userId, BigDecimal amount);
}
