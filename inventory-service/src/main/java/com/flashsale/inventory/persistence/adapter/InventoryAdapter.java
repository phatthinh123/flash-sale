package com.flashsale.inventory.persistence.adapter;

import com.flashsale.inventory.business.dto.InventoryRecord;
import com.flashsale.inventory.business.port.InventoryRepository;
import com.flashsale.inventory.persistence.entity.InventoryEntity;
import com.flashsale.inventory.persistence.jpa.JpaInventoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class InventoryAdapter implements InventoryRepository {

  private final JpaInventoryRepository jpaInventoryRepository;
  private final JpaMapper jpaMapper;

  @Override
  public List<InventoryRecord> findAll() {
    return jpaInventoryRepository.findAll().stream().map(jpaMapper::toDomain).toList();
  }

  @Override
  public Optional<InventoryRecord> findByProductId(UUID productId) {
    return jpaInventoryRepository.findByProductId(productId).map(jpaMapper::toDomain);
  }

  @Override
  public InventoryRecord save(InventoryRecord record) {
    InventoryEntity entity = jpaMapper.toEntity(record);
    return jpaMapper.toDomain(jpaInventoryRepository.save(entity));
  }

  @Override
  @Transactional
  public void decrementStock(UUID productId, int quantity) {
    jpaInventoryRepository
        .findByProductId(productId)
        .filter(entity -> entity.getStockQuantity() >= quantity)
        .ifPresent(
            entity -> {
              entity.setStockQuantity(entity.getStockQuantity() - quantity);
              entity.setLastSyncedAt(java.time.LocalDateTime.now());
              jpaInventoryRepository.save(entity);
            });
  }

  @Mapper(componentModel = "spring")
  interface JpaMapper {
    InventoryEntity toEntity(InventoryRecord record);

    InventoryRecord toDomain(InventoryEntity entity);
  }
}
