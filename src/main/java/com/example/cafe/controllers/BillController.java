package com.example.cafe.controllers;

import com.example.cafe.dto.BillDTO;
import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Bill;
import com.example.cafe.security.services.BillService;
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
@RequestMapping("/api/bills")
public class BillController {

    private final BillService service;

    private static final int MAX_PAGE_SIZE = 100;

    public BillController(BillService service) {
        this.service = service;
    }

    // ============ SỬA: phân trang + trả DTO ============
    @GetMapping
    public ResponseEntity<PageResponse<BillDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String paymentMethod,
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

        // Service trả PageResponse<Bill>
        PageResponse<Bill> entityPage = service.getBillsPaged(
                keyword, paymentStatus, paymentMethod, fromDate, toDate, pageable
        );

        // Map Bill → BillDTO
        List<BillDTO> dtoList = entityPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        PageResponse<BillDTO> dtoPage = PageResponse.<BillDTO>builder()
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
        return ResponseEntity.ok(service.getBillStats());
    }

    // ============ CŨ: giữ nguyên ============
    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(bill -> ResponseEntity.ok(toDTO(bill)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Bill> create(@RequestBody Bill b) {
        return ResponseEntity.ok(service.save(b));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bill> update(@PathVariable Long id, @RequestBody Bill b) {
        return ResponseEntity.ok(service.update(id, b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============ HELPER: Bill → BillDTO ============
    private BillDTO toDTO(Bill bill) {
        return BillDTO.builder()
                .id(bill.getId())
                .orderId(bill.getOrder() != null ? bill.getOrder().getId() : null)
                .totalAmount(bill.getTotalAmount())
                .paymentMethod(bill.getPaymentMethod())
                .paymentStatus(bill.getPaymentStatus())
                .notes(bill.getNotes())
                .issuedAt(bill.getIssuedAt())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}