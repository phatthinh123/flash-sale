package com.flashsale.flashsale.integration.event;

import com.flashsale.flashsale.business.port.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingEventPublisherAdapter implements EventPublisherPort {

  @Override
  public void publish(String topic, Object payload) {
    log.info("Publishing event topic={}, payload={}", topic, payload);
  }
}
