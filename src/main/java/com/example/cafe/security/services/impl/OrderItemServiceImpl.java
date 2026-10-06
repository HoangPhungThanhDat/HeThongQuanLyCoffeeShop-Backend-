package com.example.cafe.security.services.impl;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.OrderItem;
import com.example.cafe.repository.OrderItemRepository;
import com.example.cafe.security.services.OrderItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository repo;

    public OrderItemServiceImpl(OrderItemRepository repo) {
        this.repo = repo;
    }

    // ============ CŨ: giữ nguyên ============
    @Override
    public OrderItem save(OrderItem item) {
        return repo.save(item);
    }

    @Override
    public OrderItem update(Long id, OrderItem item) {
        return repo.findById(id).map(existing -> {
            existing.setQuantity(item.getQuantity());
            existing.setPrice(item.getPrice());
            existing.setSubtotal(item.getSubtotal());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("OrderItem not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<OrderItem> findAll() {
        return repo.findAll();
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return repo.findByOrderId(orderId);
    }

    // ============ MỚI 1: phân trang ============
    @Override
    public PageResponse<OrderItem> getOrderItemsPaged(
            String keyword,
            Long orderId,
            Long productId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<OrderItem> page = repo.searchOrderItems(kw, orderId, productId, fromDate, toDate, pageable);
        return PageResponse.from(page, oi -> oi);
    }

    // ============ MỚI 2: thống kê ============
    @Override
    public Map<String, Object> getOrderItemStats() {
        return repo.getOrderItemStats();
    }
}