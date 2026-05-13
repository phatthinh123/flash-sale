package com.flashsale.inventory.integration.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.inventory.business.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listens to the "flashsale.purchase.completed" Redis Stream and updates inventory.
 * Consumer group guarantees at-least-once delivery; idempotency is enforced in
 * {@link InventoryService#handlePurchase} via the processed_orders table.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PurchaseEventConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String payload = message.getValue().get("payload");
            JsonNode json = objectMapper.readTree(payload);

            UUID productId = UUID.fromString(json.get("productId").asText());
            String name = json.get("productName").asText();
            int quantity = json.get("quantity").asInt();
            String orderId = json.get("orderId").asText();

            inventoryService.handlePurchase(productId, name, quantity, orderId);
        } catch (Exception e) {
            log.error("Failed to process purchase event id={}", message.getId(), e);
        }
    }
}
