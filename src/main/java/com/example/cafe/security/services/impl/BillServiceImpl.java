package com.example.cafe.security.services.impl;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Bill;
import com.example.cafe.repository.BillRepository;
import com.example.cafe.security.services.BillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository repo;

    public BillServiceImpl(BillRepository repo) {
        this.repo = repo;
    }

    // ============ CŨ: giữ nguyên ============
    @Override
    public Bill save(Bill b) {
        return repo.save(b);
    }

    @Override
    public Bill update(Long id, Bill b) {
        return repo.findById(id).map(existing -> {
            existing.setTotalAmount(b.getTotalAmount());
            existing.setPaymentMethod(b.getPaymentMethod());
            existing.setPaymentStatus(b.getPaymentStatus());
            existing.setIssuedAt(b.getIssuedAt());
            existing.setNotes(b.getNotes());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<Bill> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Bill> findAll() {
        return repo.findAll();
    }

    // ============ MỚI 1: phân trang ============
    @Override
    public PageResponse<Bill> getBillsPaged(
            String keyword,
            String paymentStatus,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String ps = (paymentStatus == null || paymentStatus.isBlank()) ? null : paymentStatus;
        String pm = (paymentMethod == null || paymentMethod.isBlank()) ? null : paymentMethod;

        Page<Bill> page = repo.searchBills(kw, ps, pm, fromDate, toDate, pageable);
        return PageResponse.from(page, b -> b);
    }

    // ============ MỚI 2: thống kê ============
    @Override
    public Map<String, Object> getBillStats() {
        return repo.getBillStats();
    }
}