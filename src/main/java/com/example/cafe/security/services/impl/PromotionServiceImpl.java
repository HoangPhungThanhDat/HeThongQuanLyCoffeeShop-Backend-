package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Product;
import com.example.cafe.entity.Promotion;
import com.example.cafe.repository.ProductRepository;
import com.example.cafe.repository.PromotionRepository;
import com.example.cafe.security.services.PromotionService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository repo;
    private final ProductRepository productRepo;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    public PromotionServiceImpl(PromotionRepository repo,
                                ProductRepository productRepo,
                                ActivityLogService activityLogService) {   // 👈 MỚI
        this.repo = repo;
        this.productRepo = productRepo;
        this.activityLogService = activityLogService;
    }

    // ==================== SAVE ====================
    @Override
    public Promotion save(Promotion p) {
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
        Promotion saved = repo.save(p);

        try {
            String discount = buildDiscountText(saved);
            int productCount = saved.getProducts() != null ? saved.getProducts().size() : 0;

            activityLogService.logCreate(
                    "Khuyến mãi",
                    saved.getName(),
                    "Thêm khuyến mãi mới",
                    String.format("Tạo khuyến mãi '%s' (%s) — áp dụng cho %d sản phẩm, từ %s đến %s",
                            saved.getName(),
                            discount,
                            productCount,
                            saved.getStartDate(),
                            saved.getEndDate())
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE promotion: " + e.getMessage());
        }

        return saved;
    }

    // ==================== UPDATE ====================
    @Override
    public Promotion update(Long id, Promotion p) {
        Promotion old = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        String oldName = old.getName();
        BigDecimal oldPercent = old.getDiscountPercentage();
        BigDecimal oldAmount = old.getDiscountAmount();
        Boolean oldActive = old.getIsActive();

        Promotion updated = repo.findById(id).map(existing -> {
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

        try {
            StringBuilder changes = new StringBuilder();
            if (oldName != null && !oldName.equals(updated.getName())) {
                changes.append("Tên: '").append(oldName).append("' → '")
                        .append(updated.getName()).append("'. ");
            }
            if (oldPercent != null && !oldPercent.equals(updated.getDiscountPercentage())) {
                changes.append("Giảm %: ").append(oldPercent).append(" → ")
                        .append(updated.getDiscountPercentage()).append(". ");
            }
            if (oldAmount != null && !oldAmount.equals(updated.getDiscountAmount())) {
                changes.append(String.format("Giảm tiền: %,.0fđ → %,.0fđ. ",
                        oldAmount, updated.getDiscountAmount()));
            }
            if (oldActive != null && !oldActive.equals(updated.getIsActive())) {
                changes.append("Trạng thái: ")
                        .append(oldActive ? "Bật" : "Tắt")
                        .append(" → ")
                        .append(updated.getIsActive() ? "Bật" : "Tắt")
                        .append(". ");
            }

            activityLogService.logUpdate(
                    "Khuyến mãi",
                    updated.getName(),
                    "Cập nhật khuyến mãi",
                    changes.length() > 0
                            ? changes.toString().trim()
                            : "Cập nhật khuyến mãi '" + updated.getName() + "'"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log UPDATE promotion: " + e.getMessage());
        }

        return updated;
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long id) {
        Promotion p = repo.findById(id).orElse(null);
        String name = p != null ? p.getName() : "ID " + id;

        repo.deleteById(id);

        try {
            activityLogService.logDelete(
                    "Khuyến mãi",
                    name,
                    "Xóa khuyến mãi",
                    "Đã xóa khuyến mãi '" + name + "' khỏi hệ thống"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE promotion: " + e.getMessage());
        }
    }

    @Override
    public Optional<Promotion> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Promotion> findAll() {
        return repo.findAll();
    }

    // ==================== HELPER ====================
    private String buildDiscountText(Promotion p) {
        StringBuilder sb = new StringBuilder();
        if (p.getDiscountPercentage() != null
                && p.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("giảm ").append(p.getDiscountPercentage()).append("%");
        }
        if (p.getDiscountAmount() != null
                && p.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(String.format("giảm %,.0fđ", p.getDiscountAmount()));
        }
        return sb.length() > 0 ? sb.toString() : "không giảm giá";
    }
}