package com.flashsale.inventory.configuration;

import com.flashsale.inventory.integration.event.PurchaseEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisStreamConfig {

    private static final String STREAM_KEY    = "flashsale.purchase.completed";
    private static final String CONSUMER_GROUP = "inventory-group";
    private static final String CONSUMER_NAME  = "inventory-consumer-1";

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(
            RedisConnectionFactory connectionFactory,
            StringRedisTemplate redisTemplate,
            PurchaseEventConsumer purchaseEventConsumer) {

        createConsumerGroupIfAbsent(redisTemplate);

        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(2))
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        Subscription subscription = container.receive(
                Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                purchaseEventConsumer);

        container.start();
        log.info("Redis Streams consumer started stream={} group={}", STREAM_KEY, CONSUMER_GROUP);
        return container;
    }

    private void createConsumerGroupIfAbsent(StringRedisTemplate redisTemplate) {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.from("0"), CONSUMER_GROUP);
        } catch (Exception e) {
            // Group likely already exists — safe to ignore
            log.debug("Consumer group '{}' already exists or stream not yet created", CONSUMER_GROUP);
        }
    }
}
