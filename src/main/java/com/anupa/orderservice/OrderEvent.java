package com.anupa.orderservice;
import lombok.Data;

@Data
public class OrderEvent {
    String orderId;
    String eventType; // CREATED, PAYMENT_RECEIVED, DELIVERED, CANCELLED, REJECTED
    String traceId;
}
