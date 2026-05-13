package com.flashsale.inventory.business.service;

import com.flashsale.inventory.business.dto.InventoryRecord;
import com.flashsale.inventory.business.port.InventoryRepository;
import com.flashsale.inventory.persistence.entity.ProcessedOrderEntity;
import com.flashsale.inventory.persistence.jpa.JpaProcessedOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final JpaProcessedOrderRepository processedOrderRepository;

    public List<InventoryRecord> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Handles a purchase event from the flash-sale service.
     * Idempotent: duplicate orderId events are silently skipped.
     */
    @Transactional
    public void handlePurchase(UUID productId, String productName, int quantity, String orderId) {
        if (processedOrderRepository.existsById(orderId)) {
            log.warn("Duplicate purchase event skipped orderId={}", orderId);
            return;
        }

        inventoryRepository.decrementStock(productId, quantity);
        processedOrderRepository.save(ProcessedOrderEntity.builder().orderId(orderId).build());

        log.info("Inventory decremented product={} qty={} orderId={}", productName, quantity, orderId);
    }
}
