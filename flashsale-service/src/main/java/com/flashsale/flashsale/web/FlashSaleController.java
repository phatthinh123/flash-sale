package com.flashsale.flashsale.web;

import com.flashsale.flashsale.business.dto.FlashSaleProduct;
import com.flashsale.flashsale.business.dto.PurchaseOrder;
import com.flashsale.flashsale.business.port.FlashSalePort;
import com.flashsale.flashsale.web.dto.FlashSaleProductResponse;
import com.flashsale.flashsale.web.dto.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flash-sales")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Flash Sales", description = "Flash sale product listing and purchase")
public class FlashSaleController {

  private final FlashSalePort flashSalePort;
  private final FlashSaleMapper flashSaleMapper;

  @GetMapping("/active")
  @Operation(
      summary = "Get active flash sale products",
      description = "Returns all products currently on flash sale")
  public ResponseEntity<List<FlashSaleProductResponse>> getActiveProducts() {
    List<FlashSaleProductResponse> products =
        flashSalePort.getActiveFlashSaleProducts().stream()
            .map(flashSaleMapper::toFlashSaleProductResponse)
            .toList();
    return ResponseEntity.ok(products);
  }

  @PostMapping("/{productId}/purchase")
  @Operation(
      summary = "Purchase a flash sale product",
      description =
          "Purchase a product during an active flash sale. Each user can only purchase 1 product per day.",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<PurchaseResponse> purchaseProduct(
      @PathVariable UUID productId, Authentication authentication) {
    UUID userId = UUID.fromString((String) authentication.getPrincipal());
    PurchaseOrder order = flashSalePort.purchaseProduct(userId, productId);
    return ResponseEntity.ok(flashSaleMapper.toPurchaseResponse(order));
  }

  @Mapper(componentModel = "spring")
  interface FlashSaleMapper {
    @Mapping(target = "discountPercent", expression = "java(product.discountPercent())")
    @Mapping(target = "available", expression = "java(product.isAvailable())")
    FlashSaleProductResponse toFlashSaleProductResponse(FlashSaleProduct product);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "productId", source = "flashSaleProductId")
    @Mapping(target = "status", expression = "java(purchaseOrder.status().name())")
    @Mapping(target = "message", constant = "Purchase completed successfully")
    PurchaseResponse toPurchaseResponse(PurchaseOrder purchaseOrder);
  }
}
