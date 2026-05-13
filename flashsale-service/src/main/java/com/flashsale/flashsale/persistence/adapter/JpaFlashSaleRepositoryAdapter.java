package com.flashsale.flashsale.persistence.adapter;

import com.flashsale.flashsale.business.dto.FlashSaleProduct;
import com.flashsale.flashsale.business.dto.FlashSaleSlot;
import com.flashsale.flashsale.business.port.FlashSaleRepository;
import com.flashsale.flashsale.persistence.entity.FlashSaleProductEntity;
import com.flashsale.flashsale.persistence.entity.FlashSaleSlotEntity;
import com.flashsale.flashsale.persistence.jpa.JpaFlashSaleProductRepository;
import com.flashsale.flashsale.persistence.jpa.JpaFlashSaleSlotRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaFlashSaleRepositoryAdapter implements FlashSaleRepository {

    private final JpaFlashSaleSlotRepository slotRepository;
    private final JpaFlashSaleProductRepository productRepository;
    private final JpaMapper jpaMapper;

    @Override
    public List<FlashSaleSlot> findActiveSlots() {
        return slotRepository.findByActiveTrueAndSaleDate(LocalDate.now())
                .stream()
                .map(jpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<FlashSaleProduct> findProductsBySlotIds(List<UUID> slotIds) {
        if (slotIds.isEmpty()) {
            return List.of();
        }
        return productRepository.findBySlotIdIn(slotIds)
                .stream()
                .map(jpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FlashSaleProduct> findProductById(UUID productId) {
        return productRepository.findById(productId).map(jpaMapper::toDomain);
    }

    @Override
    public Optional<FlashSaleSlot> findSlotById(UUID slotId) {
        return slotRepository.findById(slotId).map(jpaMapper::toDomain);
    }

    /**
     * Decrements remaining quantity using JPA optimistic locking (@Version).
     * Returns false if the product is out of stock, not found, or a concurrent
     * update was detected (version mismatch → ObjectOptimisticLockingFailureException).
     */
    @Override
    @Transactional
    public boolean decrementQuantity(UUID productId, int currentVersion) {
        try {
            return productRepository.findById(productId)
                    .filter(e -> e.getVersion() == currentVersion && e.getRemainingQuantity() > 0)
                    .map(e -> {
                        e.setRemainingQuantity(e.getRemainingQuantity() - 1);
                        productRepository.save(e);
                        return true;
                    })
                    .orElse(false);
        } catch (ObjectOptimisticLockingFailureException e) {
            return false;
        }
    }

    @Mapper(componentModel = "spring")
    interface JpaMapper {
        FlashSaleSlot toDomain(FlashSaleSlotEntity entity);

        FlashSaleProduct toDomain(FlashSaleProductEntity entity);
    }
}


