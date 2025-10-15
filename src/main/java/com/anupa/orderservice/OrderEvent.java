package com.anupa.orderservice;
import lombok.Data;

@Data
public class OrderEvent {
    String orderId;
    String eventType; // CREATED, PAYMENT_RECEIVED, DELIVERED, CANCELLED, REJECTED
    String traceId;

    public OrderEvent(String s, String s1, String s2) {
    }

    public OrderEvent() {
    }
}
