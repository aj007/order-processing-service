package com.anupa.orderservice;

import com.anupa.orderservice.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Order order) {
        kafkaTemplate.send(TOPIC, "Order Created: " + order.getId());
    }

    public void publishOrderStatusUpdated(Order order) {
        kafkaTemplate.send(TOPIC, "Order Updated: " + order.getId() + " Status: " + order.getStatus());
    }
}
