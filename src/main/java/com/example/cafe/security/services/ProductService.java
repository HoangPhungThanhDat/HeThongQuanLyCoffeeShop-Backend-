package com.example.cafe.security.services;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductService {

    // ===== CŨ: giữ nguyên =====
    Product save(Product p);
    Product update(Long id, Product p);
    void delete(Long id);
    Optional<Product> findById(Long id);
    List<Product> findAll();

    String saveImage(MultipartFile file);
    InputStream getImage(String fileName) throws FileNotFoundException;

    List<Product> getProductsByCategory(Long categoryId);
    List<Product> getNewestProducts();
    Product reduceStock(Long id, int qty);
    List<Product> getProductsWithActivePromotions();

    // ===== MỚI =====
    PageResponse<Product> getProductsPaged(String keyword, Long categoryId, Pageable pageable);
    Map<String, Object> getProductStats();

    // ===== MỚI: cho Menu — lấy toàn bộ sản phẩm đang bán =====
    List<Product> findAllActive();
}