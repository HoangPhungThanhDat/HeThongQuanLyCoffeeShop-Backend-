package com.example.cafe.security.services;

import com.example.cafe.entity.Promotion;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    Promotion save(Promotion p);
    Promotion update(Long id, Promotion p);
    void delete(Long id);
    Optional<Promotion> findById(Long id);
    List<Promotion> findAll();
}
