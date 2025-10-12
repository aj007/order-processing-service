package com.anupa.orderservice;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {
    @Id
    private String id;
    private String customerId;
    private String productId;
    private Integer totalAmount;
    private String status;
    private Instant createdAt = Instant.now();
}
