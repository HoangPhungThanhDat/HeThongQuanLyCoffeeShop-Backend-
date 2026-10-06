package com.example.cafe.security.services;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Order;
import com.example.cafe.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {

    // ============ CŨ: giữ nguyên ============
    Order save(Order o);
    Order update(Long id, Order o);
    void delete(Long id);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    Order addItemsToOrder(Long orderId, List<OrderItemDTO> itemDTOs);

    Order getOrderById(Long id);
    Order updateOrderStatus(Long id, OrderStatus status);

    // ============ MỚI ============
    PageResponse<Order> getOrdersPaged(
            String keyword,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long tableId,
            Pageable pageable
    );

    Map<String, Object> getOrderStats();
}