package com.example.cafe.security.services;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.OrderItem;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderItemService {

    // ============ CŨ: giữ nguyên ============
    OrderItem save(OrderItem item);
    OrderItem update(Long id, OrderItem item);
    void delete(Long id);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findAll();
    List<OrderItem> findByOrderId(Long orderId);

    // ============ MỚI ============
    PageResponse<OrderItem> getOrderItemsPaged(
            String keyword,
            Long orderId,
            Long productId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    Map<String, Object> getOrderItemStats();
}