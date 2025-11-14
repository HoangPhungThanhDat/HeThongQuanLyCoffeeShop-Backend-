package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Bill;
import com.example.cafe.repository.BillRepository;
import com.example.cafe.security.services.BillService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillServiceImpl implements BillService {
    private final BillRepository repo;

    public BillServiceImpl(BillRepository repo) { this.repo = repo; }

    @Override
    public Bill save(Bill b) { return repo.save(b); }

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
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    public Optional<Bill> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Bill> findAll() { return repo.findAll(); }
}