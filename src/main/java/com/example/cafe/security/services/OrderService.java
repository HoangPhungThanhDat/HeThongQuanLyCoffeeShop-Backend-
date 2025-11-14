package com.example.cafe.security.services;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.entity.Order;
import com.example.cafe.entity.enums.OrderStatus; // ✅ Import enum
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order save(Order o);
    Order update(Long id, Order o);
    void delete(Long id);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    Order addItemsToOrder(Long orderId, List<OrderItemDTO> itemDTOs);
    
    Order getOrderById(Long id);
    Order updateOrderStatus(Long id, OrderStatus status);
}