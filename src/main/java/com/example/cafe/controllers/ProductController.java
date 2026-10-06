package com.example.cafe.controllers;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Product;
import com.example.cafe.security.services.ProductService;
import com.example.cafe.services.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductService service, CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    // ===== SỬA: phân trang (dành cho Admin) =====
    @GetMapping
    public ResponseEntity<PageResponse<Product>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        return ResponseEntity.ok(service.getProductsPaged(keyword, categoryId, pageable));
    }

    // ===== MỚI: cho Menu — lấy toàn bộ sản phẩm đang bán (không phân trang) =====
    @GetMapping("/menu")
    public ResponseEntity<List<Product>> getMenuProducts() {
        return ResponseEntity.ok(service.findAllActive());
    }

    // ===== MỚI: thống kê =====
    // ⚠️ PHẢI đặt TRƯỚC /{id} để tránh Spring match "stats" thành id
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getProductStats());
    }

    // ===== CŨ: giữ nguyên =====
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> create(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                product.setImageUrl(imageUrl);
            } else {
                product.setImageUrl("default.png");
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(service.save(product));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                product.setImageUrl(imageUrl);
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(service.update(id, product));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    private ProductService productService;

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/newest")
    public List<Product> getNewestProducts() {
        return productService.getNewestProducts();
    }

    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(
            @PathVariable Long id,
            @RequestParam int qty) {
        try {
            Product updated = service.reduceStock(id, qty);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/with-promotions")
    public ResponseEntity<List<Product>> getProductsWithPromotions() {
        List<Product> products = productService.getProductsWithActivePromotions();
        return ResponseEntity.ok(products);
    }
}