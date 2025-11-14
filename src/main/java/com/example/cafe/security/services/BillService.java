package com.example.cafe.security.services;

import com.example.cafe.entity.Bill;

import java.util.List;
import java.util.Optional;

public interface BillService {
    Bill save(Bill b);
    Bill update(Long id, Bill b);
    void delete(Long id);
    Optional<Bill> findById(Long id);
    List<Bill> findAll();
}