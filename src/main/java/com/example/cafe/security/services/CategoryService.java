package com.example.cafe.security.services;

import com.example.cafe.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category save(Category c);
    Category update(Long id, Category c);
    void delete(Long id);
    Optional<Category> findById(Long id);
    List<Category> findAll();
}