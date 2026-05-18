package com.flashsale.flashsale.integration.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.flashsale.business.port.EventPublisherPort;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to Redis Streams so the inventory-service can consume them asynchronously
 * and update stock levels.
 */
@Primary
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisStreamsEventPublisherAdapter implements EventPublisherPort {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(String topic, Object payload) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      redisTemplate.opsForStream().add(MapRecord.create(topic, Map.of("payload", json)));
      log.debug("Published event to stream={}", topic);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize event for topic={}", topic, e);
      throw new IllegalStateException("Failed to publish event", e);
    }
  }
}
