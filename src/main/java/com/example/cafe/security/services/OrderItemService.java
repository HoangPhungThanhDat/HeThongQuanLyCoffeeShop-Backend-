package com.example.cafe.security.services;

import com.example.cafe.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {
    OrderItem save(OrderItem item);
    OrderItem update(Long id, OrderItem item);
    void delete(Long id);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findAll();
    List<OrderItem> findByOrderId(Long orderId);

}