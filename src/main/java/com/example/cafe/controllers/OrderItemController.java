package com.example.cafe.controllers;

import com.example.cafe.dto.OrderItemDTO;
import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.OrderItem;
import com.example.cafe.security.services.OrderItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService service;

    private static final int MAX_PAGE_SIZE = 100;

    public OrderItemController(OrderItemService service) {
        this.service = service;
    }

    // ============ SỬA: phân trang + trả DTO ============
    @GetMapping
    public ResponseEntity<PageResponse<OrderItemDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        PageResponse<OrderItem> entityPage = service.getOrderItemsPaged(
                keyword, orderId, productId, fromDate, toDate, pageable
        );

        // Map entity → DTO
        List<OrderItemDTO> dtoList = entityPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        PageResponse<OrderItemDTO> dtoPage = PageResponse.<OrderItemDTO>builder()
                .content(dtoList)
                .page(entityPage.getPage())
                .size(entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages())
                .first(entityPage.isFirst())
                .last(entityPage.isLast())
                .hasNext(entityPage.isHasNext())
                .hasPrevious(entityPage.isHasPrevious())
                .build();

        return ResponseEntity.ok(dtoPage);
    }

    // ============ MỚI: stats ============
    // ⚠️ PHẢI đặt TRƯỚC /{id}
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getOrderItemStats());
    }

    // ============ CŨ: giữ nguyên ============
    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(item -> ResponseEntity.ok(toDTO(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderItem> create(@RequestBody OrderItem item) {
        return ResponseEntity.ok(service.save(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> update(@PathVariable Long id, @RequestBody OrderItem item) {
        return ResponseEntity.ok(service.update(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getByOrderId(@PathVariable Long orderId) {
        List<OrderItem> items = service.findByOrderId(orderId);
        return ResponseEntity.ok(items);
    }

    // ============ HELPER ============
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
}