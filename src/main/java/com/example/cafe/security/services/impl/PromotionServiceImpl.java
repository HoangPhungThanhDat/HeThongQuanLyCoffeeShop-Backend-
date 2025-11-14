package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Product;
import com.example.cafe.entity.Promotion;
import com.example.cafe.repository.ProductRepository;
import com.example.cafe.repository.PromotionRepository;
import com.example.cafe.security.services.PromotionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository repo;
    private final ProductRepository productRepo;

    public PromotionServiceImpl(PromotionRepository repo, ProductRepository productRepo) {
        this.repo = repo;
        this.productRepo = productRepo;
    }

    @Override
    public Promotion save(Promotion p) {
        // Gắn Product thật từ DB vào Promotion
        if (p.getProducts() != null && !p.getProducts().isEmpty()) {
            Set<Product> attached = new HashSet<>();
            for (Product prod : p.getProducts()) {
                if (prod.getId() != null) {
                    Product found = productRepo.findById(prod.getId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + prod.getId()));
                    attached.add(found);
                }
            }
            p.setProducts(attached);
        }

        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return repo.save(p);
    }

    @Override
    public Promotion update(Long id, Promotion p) {
        return repo.findById(id).map(existing -> {
            existing.setName(p.getName());
            existing.setDiscountPercentage(p.getDiscountPercentage());
            existing.setDiscountAmount(p.getDiscountAmount());
            existing.setStartDate(p.getStartDate());
            existing.setEndDate(p.getEndDate());
            existing.setIsActive(p.getIsActive());
            existing.setUpdatedAt(LocalDateTime.now());

            if (p.getProducts() != null) {
                Set<Product> attached = new HashSet<>();
                for (Product prod : p.getProducts()) {
                    if (prod.getId() != null) {
                        Product found = productRepo.findById(prod.getId())
                                .orElseThrow(() -> new RuntimeException("Product not found: " + prod.getId()));
                        attached.add(found);
                    }
                }
                existing.setProducts(attached);
            }

            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<Promotion> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Promotion> findAll() {
        return repo.findAll();
    }
}
