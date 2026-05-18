package com.flashsale.flashsale.persistence.jpa;

import com.flashsale.flashsale.persistence.entity.FlashSaleProductEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFlashSaleProductRepository extends JpaRepository<FlashSaleProductEntity, UUID> {
  List<FlashSaleProductEntity> findBySlotIdIn(Collection<UUID> slotIds);
}
