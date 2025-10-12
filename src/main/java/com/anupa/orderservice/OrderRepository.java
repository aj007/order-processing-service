package com.anupa.orderservice;

import com.anupa.orderservice.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
