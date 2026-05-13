package com.flashsale.auth.business.port;

import com.flashsale.auth.business.dto.User;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void updateVerificationStatus(UUID userId, String targetType, boolean verified);

    void updateBalance(UUID userId, BigDecimal newBalance);

    boolean debitBalance(UUID userId, BigDecimal amount);
}
