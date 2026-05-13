package com.flashsale.flashsale.business.port;

import com.flashsale.flashsale.business.dto.FlashSaleProduct;
import com.flashsale.flashsale.business.dto.FlashSaleSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlashSaleRepository {
    List<FlashSaleSlot> findActiveSlots();
    List<FlashSaleProduct> findProductsBySlotIds(List<UUID> slotIds);

    Optional<FlashSaleProduct> findProductById(UUID productId);

    Optional<FlashSaleSlot> findSlotById(UUID slotId);

    /**
     * Decrement remaining quantity with optimistic locking.
     *
     * @return true if the update was successful (version matched and quantity > 0)
     */
    boolean decrementQuantity(UUID productId, int currentVersion);

}
