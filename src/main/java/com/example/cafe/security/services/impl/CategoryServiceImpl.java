package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Category;
import com.example.cafe.repository.CategoryRepository;
import com.example.cafe.security.services.CategoryService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    public CategoryServiceImpl(CategoryRepository repo,
                               ActivityLogService activityLogService) {   // 👈 MỚI
        this.repo = repo;
        this.activityLogService = activityLogService;
    }

    // ==================== SAVE ====================
    @Override
    public Category save(Category c) {
        Category saved = repo.save(c);

        try {
            activityLogService.logCreate(
                    "Danh mục",
                    saved.getName(),
                    "Thêm danh mục mới",
                    "Đã tạo danh mục '" + saved.getName() + "'"
                            + (saved.getDescription() != null
                                    ? " — " + saved.getDescription()
                                    : "")
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE category: " + e.getMessage());
        }

        return saved;
    }

    // ==================== UPDATE ====================
    @Override
    public Category update(Long id, Category c) {
        Category old = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String oldName = old.getName();
        String oldDesc = old.getDescription();

        Category updated = repo.findById(id).map(existing -> {
            existing.setName(c.getName());
            existing.setDescription(c.getDescription());
            existing.setImageUrl(c.getImageUrl());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Category not found"));

        try {
            StringBuilder changes = new StringBuilder();
            if (oldName != null && !oldName.equals(updated.getName())) {
                changes.append("Tên: '").append(oldName).append("' → '")
                        .append(updated.getName()).append("'. ");
            }
            if (oldDesc != null && !oldDesc.equals(updated.getDescription())) {
                changes.append("Mô tả đã thay đổi. ");
            }

            activityLogService.logUpdate(
                    "Danh mục",
                    updated.getName(),
                    "Cập nhật danh mục",
                    changes.length() > 0
                            ? changes.toString().trim()
                            : "Cập nhật danh mục '" + updated.getName() + "'"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log UPDATE category: " + e.getMessage());
        }

        return updated;
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long id) {
        Category c = repo.findById(id).orElse(null);
        String name = c != null ? c.getName() : "ID " + id;

        repo.deleteById(id);

        try {
            activityLogService.logDelete(
                    "Danh mục",
                    name,
                    "Xóa danh mục",
                    "Đã xóa danh mục '" + name + "' khỏi hệ thống"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE category: " + e.getMessage());
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return repo.findAll();
    }
}