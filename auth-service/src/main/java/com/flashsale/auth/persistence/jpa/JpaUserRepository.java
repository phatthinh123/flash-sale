package com.flashsale.auth.persistence.jpa;

import com.flashsale.auth.persistence.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findForUpdateById(UUID id);
}
