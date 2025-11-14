package com.example.cafe.controllers;

import com.example.cafe.dto.BillDTO;
import com.example.cafe.entity.Bill;
import com.example.cafe.security.services.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    private final BillService service;

    public BillController(BillService service) {
        this.service = service;
    }

    // ✅ Lấy tất cả Bill — chỉ trả về DTO, không lặp dữ liệu
    @GetMapping
    public ResponseEntity<List<BillDTO>> getAll() {
        List<BillDTO> dtos = service.findAll().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ✅ Lấy 1 Bill theo ID — trả về DTO
    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(bill -> ResponseEntity.ok(toDTO(bill)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ POST, PUT, DELETE vẫn dùng entity
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

    // ✅ Helper: Chuyển Bill → BillDTO
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
