package com.flashsale.inventory.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryRecord(
    UUID id, UUID productId, String productName, int stockQuantity, LocalDateTime lastSyncedAt) {}
