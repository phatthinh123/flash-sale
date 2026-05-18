package com.flashsale.flashsale.persistence.jpa;

import com.flashsale.flashsale.persistence.entity.FlashSaleSlotEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFlashSaleSlotRepository extends JpaRepository<FlashSaleSlotEntity, UUID> {
  List<FlashSaleSlotEntity> findByActiveTrueAndSaleDate(LocalDate saleDate);
}
