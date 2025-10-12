package com.anupa.orderservice;

import com.anupa.orderservice.OrderEventPublisher;
import com.anupa.orderservice.Order;
import com.anupa.orderservice.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventPublisher eventPublisher;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCreated(saved);
        return saved;
    }

    public Order updateOrderStatus(String id, String status) {
        Order existing = orderRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setStatus(status);
            Order updated = orderRepository.save(existing);
            eventPublisher.publishOrderStatusUpdated(updated);
            return updated;
        }
        return null;
    }
}
