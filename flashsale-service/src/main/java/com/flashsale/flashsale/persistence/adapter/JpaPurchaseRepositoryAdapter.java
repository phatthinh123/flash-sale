package com.flashsale.flashsale.persistence.adapter;

import com.flashsale.flashsale.business.dto.PurchaseOrder;
import com.flashsale.flashsale.business.port.PurchaseRepository;
import com.flashsale.flashsale.persistence.entity.PurchaseOrderEntity;
import com.flashsale.flashsale.persistence.jpa.JpaPurchaseOrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPurchaseRepositoryAdapter implements PurchaseRepository {

  private final JpaPurchaseOrderRepository purchaseOrderRepository;
  private final JpaMapper jpaMapper;

  @Override
  public boolean existsByUserAndDate(UUID userId, LocalDate purchaseDate) {
    LocalDateTime startOfDay = purchaseDate.atStartOfDay();
    LocalDateTime endOfDay = purchaseDate.plusDays(1).atStartOfDay();
    return purchaseOrderRepository.existsByUserIdAndPurchasedAtBetween(
        userId, startOfDay, endOfDay);
  }

  @Override
  public PurchaseOrder save(PurchaseOrder order) {
    PurchaseOrderEntity entity = purchaseOrderRepository.save(jpaMapper.toEntity(order));
    return jpaMapper.toDomain(entity);
  }

  @Mapper(componentModel = "spring")
  interface JpaMapper {
    PurchaseOrderEntity toEntity(PurchaseOrder order);

    PurchaseOrder toDomain(PurchaseOrderEntity entity);
  }
}
