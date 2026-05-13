package com.flashsale.inventory.web;

import com.flashsale.inventory.business.dto.InventoryRecord;
import com.flashsale.inventory.business.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Product stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "List all inventory", description = "Returns current stock levels for all tracked products")
    public ResponseEntity<List<InventoryRecord>> getAll() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
}
