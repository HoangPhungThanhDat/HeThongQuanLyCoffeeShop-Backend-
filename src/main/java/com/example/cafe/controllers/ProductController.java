package com.example.cafe.controllers;

import com.example.cafe.entity.Product;
import com.example.cafe.security.services.ProductService;
import com.example.cafe.services.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    private final CloudinaryService cloudinaryService;

    @Autowired
    private ProductService productService;

    public ProductController(ProductService service, CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Thêm sản phẩm (upload ảnh lên Cloudinary)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                // ✅ Upload lên Cloudinary
                String imageUrl = cloudinaryService.uploadImage(imageFile, "products");
                product.setImageUrl(imageUrl);
            } else {
                product.setImageUrl(null);
            }

            Product savedProduct = service.save(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    // ✅ Cập nhật sản phẩm (có thể thay ảnh trên Cloudinary)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            // Lấy sản phẩm hiện tại
            Product existingProduct = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (imageFile != null && !imageFile.isEmpty()) {
                // ✅ Xóa ảnh cũ trên Cloudinary (nếu có)
                if (existingProduct.getImageUrl() != null) {
                    cloudinaryService.deleteImage(existingProduct.getImageUrl());
                }

                // ✅ Upload ảnh mới lên Cloudinary
                String newImageUrl = cloudinaryService.uploadImage(imageFile, "products");
                product.setImageUrl(newImageUrl);
            } else {
                // Giữ nguyên ảnh cũ nếu không upload ảnh mới
                product.setImageUrl(existingProduct.getImageUrl());
            }

            Product updatedProduct = service.update(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    // ✅ Xóa sản phẩm (và xóa ảnh trên Cloudinary)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            // Lấy sản phẩm để lấy URL ảnh
            Product product = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // ✅ Xóa ảnh trên Cloudinary
            if (product.getImageUrl() != null) {
                cloudinaryService.deleteImage(product.getImageUrl());
            }

            // Xóa sản phẩm trong database
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product: " + e.getMessage());
        }
    }

    // ❌ XÓA endpoint này vì không cần nữa (ảnh đã lưu trên Cloudinary)
    // @GetMapping("/image/{fileName}")
    // public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) { ... }

    // Lấy sản phẩm theo ID danh mục
    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    // API lấy sản phẩm mới nhất
    @GetMapping("/newest")
    public List<Product> getNewestProducts() {
        return productService.getNewestProducts();
    }

    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(
            @PathVariable Long id,
            @RequestParam int qty
    ) {
        try {
            Product updated = service.reduceStock(id, qty);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Lấy sản phẩm có khuyến mãi đang hoạt động
    @GetMapping("/with-promotions")
    public ResponseEntity<List<Product>> getProductsWithPromotions() {
        List<Product> products = productService.getProductsWithActivePromotions();
        return ResponseEntity.ok(products);
    }
}