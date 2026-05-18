package com.flashsale.flashsale.business.port;

public interface EventPublisherPort {
  void publish(String topic, Object payload);
}
