package com.flashsale.flashsale.business.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FlashSaleSlot(
    UUID id, LocalDate saleDate, LocalTime startTime, LocalTime endTime, boolean active) {
  public boolean isCurrentlyActive() {
    if (!active) return false;
    LocalDate today = LocalDate.now();
    LocalTime now = LocalTime.now();
    return saleDate.equals(today) && !now.isBefore(startTime) && now.isBefore(endTime);
  }
}
