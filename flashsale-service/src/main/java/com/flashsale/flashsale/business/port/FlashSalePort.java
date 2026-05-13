package com.flashsale.flashsale.business.port;

import com.flashsale.flashsale.business.dto.FlashSaleProduct;
import com.flashsale.flashsale.business.dto.PurchaseOrder;

import java.util.List;
import java.util.UUID;

public interface FlashSalePort {
    List<FlashSaleProduct> getActiveFlashSaleProducts();

    PurchaseOrder purchaseProduct(UUID userId, UUID productId);
}
