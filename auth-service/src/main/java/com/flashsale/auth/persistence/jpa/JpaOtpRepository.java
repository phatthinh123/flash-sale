package com.flashsale.auth.persistence.jpa;

import com.flashsale.auth.persistence.entity.OtpTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOtpRepository extends JpaRepository<OtpTokenEntity, UUID> {
  Optional<OtpTokenEntity> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(UUID userId);
}
