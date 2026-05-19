package com.flashsale.flashsale.business.service;

import com.flashsale.flashsale.business.dto.FlashSaleProduct;
import com.flashsale.flashsale.business.dto.FlashSaleSlot;
import com.flashsale.flashsale.business.dto.PurchaseOrder;
import com.flashsale.flashsale.business.dto.User;
import com.flashsale.flashsale.business.port.DistributedLockPort;
import com.flashsale.flashsale.business.port.EventPublisherPort;
import com.flashsale.flashsale.business.port.FlashSalePort;
import com.flashsale.flashsale.business.port.FlashSaleRepository;
import com.flashsale.flashsale.business.port.PurchaseRepository;
import com.flashsale.flashsale.business.port.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlashSaleService implements FlashSalePort {

  private static final String LOCK_KEY_PREFIX = "flash-sale:product:";

  private final FlashSaleRepository flashSaleRepository;
  private final UserRepository userRepository;
  private final PurchaseRepository purchaseRepository;
  private final EventPublisherPort eventPublisherPort;
  private final DistributedLockPort distributedLockPort;

  @Override
  public List<FlashSaleProduct> getActiveFlashSaleProducts() {
    List<FlashSaleSlot> activeSlots = flashSaleRepository.findActiveSlots();
    if (activeSlots.isEmpty()) {
      return List.of();
    }
    List<UUID> slotIds =
        activeSlots.stream()
            .filter(FlashSaleSlot::isCurrentlyActive)
            .map(FlashSaleSlot::id)
            .toList();
    if (slotIds.isEmpty()) {
      return List.of();
    }
    return flashSaleRepository.findProductsBySlotIds(slotIds);
  }

  @Override
  @Transactional
  public PurchaseOrder purchaseProduct(UUID userId, UUID productId) {
    String lockKey = LOCK_KEY_PREFIX + productId;
    boolean lockAcquired = distributedLockPort.tryLock(lockKey);
    if (!lockAcquired) {
      throw new IllegalStateException(
          "System is busy processing this product, please try again later");
    }
    try {
      return executePurchase(userId, productId);
    } finally {
      distributedLockPort.unlock(lockKey);
    }
  }

  private PurchaseOrder executePurchase(UUID userId, UUID productId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!user.isVerified()) {
      throw new IllegalStateException("User account is not verified");
    }

    if (purchaseRepository.existsByUserAndDate(userId, LocalDate.now())) {
      throw new IllegalStateException(
          "You've already purchased a flash sale product today, see you tomorrow!");
    }

    FlashSaleProduct product =
        flashSaleRepository
            .findProductById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Flash sale product not found"));

    FlashSaleSlot slot =
        flashSaleRepository
            .findSlotById(product.slotId())
            .orElseThrow(() -> new IllegalArgumentException("Flash sale slot not found"));

    if (!slot.isCurrentlyActive()) {
      throw new IllegalStateException("This flash sale is not currently active");
    }

    if (!product.isAvailable()) {
      throw new IllegalStateException("Product is sold out");
    }

    if (user.balance().compareTo(product.flashPrice()) < 0) {
      throw new IllegalStateException("Insufficient balance");
    }

    boolean decremented = flashSaleRepository.decrementQuantity(productId, product.version());
    if (!decremented) {
      throw new IllegalStateException("Product is sold out or was just purchased by another user");
    }

    boolean debited = userRepository.debitBalance(userId, product.flashPrice());
    if (!debited) {
      throw new IllegalStateException("Insufficient balance");
    }

    PurchaseOrder order = PurchaseOrder.createCompleted(userId, productId, product.flashPrice());
    PurchaseOrder savedOrder;
    try {
      savedOrder = purchaseRepository.save(order);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalStateException(
          "You've already purchased a flash sale product today, see you tomorrow!", e);
    }

    eventPublisherPort.publish(
        "flashsale.purchase.completed",
        new PurchaseEvent(product.id(), product.productName(), 1, savedOrder.id().toString()));

    log.info(
        "Purchase completed: user={}, product={}, amount={}",
        userId,
        product.productName(),
        product.flashPrice());

    return savedOrder;
  }

  public record PurchaseEvent(UUID productId, String productName, int quantity, String orderId) {}
}
