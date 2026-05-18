package com.flashsale.flashsale.persistence.jpa;

import com.flashsale.flashsale.persistence.entity.PurchaseOrderEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {
  boolean existsByUserIdAndPurchasedAtBetween(
      UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}
