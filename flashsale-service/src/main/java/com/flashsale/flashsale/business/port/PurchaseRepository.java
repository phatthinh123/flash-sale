package com.flashsale.flashsale.business.port;

import com.flashsale.flashsale.business.dto.PurchaseOrder;

import java.time.LocalDate;
import java.util.UUID;

public interface PurchaseRepository {
	boolean existsByUserAndDate(UUID userId, LocalDate purchaseDate);

	PurchaseOrder save(PurchaseOrder order);
}
