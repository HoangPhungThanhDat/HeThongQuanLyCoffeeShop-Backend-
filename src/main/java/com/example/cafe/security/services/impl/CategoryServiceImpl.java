package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Category;
import com.example.cafe.repository.CategoryRepository;
import com.example.cafe.security.services.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) { this.repo = repo; }

    @Override
    public Category save(Category c) { return repo.save(c); }

    @Override
    public Category update(Long id, Category c) {
        return repo.findById(id).map(existing -> {
            existing.setName(c.getName());
            existing.setDescription(c.getDescription());
            existing.setImageUrl(c.getImageUrl());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    public Optional<Category> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Category> findAll() { return repo.findAll(); }
}