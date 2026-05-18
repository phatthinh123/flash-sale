package com.flashsale.inventory.persistence.jpa;

import com.flashsale.inventory.persistence.entity.ProcessedOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedOrderRepository extends JpaRepository<ProcessedOrderEntity, String> {}
