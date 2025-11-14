package com.example.cafe.security.services.impl;

import com.example.cafe.entity.OrderItem;
import com.example.cafe.repository.OrderItemRepository;
import com.example.cafe.security.services.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository repo;

    public OrderItemServiceImpl(OrderItemRepository repo) {
        this.repo = repo;
    }

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

}