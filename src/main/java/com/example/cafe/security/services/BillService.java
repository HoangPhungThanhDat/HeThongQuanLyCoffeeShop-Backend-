package com.example.cafe.security.services;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Bill;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BillService {

    // ============ CŨ: giữ nguyên ============
    Bill save(Bill b);
    Bill update(Long id, Bill b);
    void delete(Long id);
    Optional<Bill> findById(Long id);
    List<Bill> findAll();

    // ============ MỚI ============
    PageResponse<Bill> getBillsPaged(
            String keyword,
            String paymentStatus,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    Map<String, Object> getBillStats();
}