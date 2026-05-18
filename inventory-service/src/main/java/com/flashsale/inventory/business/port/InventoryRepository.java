package com.flashsale.inventory.business.port;

import com.flashsale.inventory.business.dto.InventoryRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
  List<InventoryRecord> findAll();

  Optional<InventoryRecord> findByProductId(UUID productId);

  InventoryRecord save(InventoryRecord record);

  /** Decrement stock for a given productId. A no-op if the product is not tracked. */
  void decrementStock(UUID productId, int quantity);
}
