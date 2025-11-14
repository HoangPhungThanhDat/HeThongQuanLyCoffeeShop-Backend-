package com.example.cafe.controllers;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.entity.OrderItem;
import com.example.cafe.security.services.OrderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService service;

    public OrderItemController(OrderItemService service) {
        this.service = service;
    }

    // ✅ Lấy toàn bộ danh sách OrderItem (trả về DTO)
    @GetMapping
    public ResponseEntity<List<OrderItemDTO>> getAll() {
        List<OrderItemDTO> dtos = service.findAll().stream()
            .map(this::toDTO)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    // ✅ Lấy 1 OrderItem theo id (trả về DTO)
    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getOne(@PathVariable Long id) {
        return service.findById(id)
            .map(item -> ResponseEntity.ok(toDTO(item)))
            .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Hàm chuyển từ Entity -> DTO
    private OrderItemDTO toDTO(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
            .productId(item.getProduct() != null ? item.getProduct().getId() : null)
            .productName(item.getProduct() != null ? item.getProduct().getName() : null)
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }

    // ✅ Tạo mới
    @PostMapping
    public ResponseEntity<OrderItem> create(@RequestBody OrderItem item) {
        return ResponseEntity.ok(service.save(item));
    }

    // ✅ Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> update(@PathVariable Long id, @RequestBody OrderItem item) {
        return ResponseEntity.ok(service.update(id, item));
    }

    // ✅ Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }






    
    // 🟢 API mới để lấy sản phẩm theo mã đơn hàng
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getByOrderId(@PathVariable Long orderId) {
        List<OrderItem> items = service.findByOrderId(orderId);
        return ResponseEntity.ok(items);
    }
}
