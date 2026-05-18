package com.flashsale.inventory.persistence.jpa;

import com.flashsale.inventory.persistence.entity.InventoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
  Optional<InventoryEntity> findByProductId(UUID productId);
}
