package com.flashsale.flashsale.persistence.jpa;

import com.flashsale.flashsale.persistence.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface JpaPurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {
    boolean existsByUserIdAndPurchasedAtBetween(UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

