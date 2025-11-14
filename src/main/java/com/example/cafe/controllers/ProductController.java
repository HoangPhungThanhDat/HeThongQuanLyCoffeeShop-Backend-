package com.example.cafe.controllers;

import com.example.cafe.entity.Product;
import com.example.cafe.security.services.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
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

    // ✅ Thêm sản phẩm (kèm upload ảnh)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> create(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = service.saveImage(imageFile);
            product.setImageUrl(fileName);
        } else {
            product.setImageUrl("default.png");
        }

        return ResponseEntity.ok(service.save(product));
    }

    // ✅ Cập nhật sản phẩm (có thể thay ảnh)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = service.saveImage(imageFile);
            product.setImageUrl(fileName);
        }

        return ResponseEntity.ok(service.update(id, product));
    }

    // ✅ Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) throws FileNotFoundException {
        InputStream imageStream = service.getImage(fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("inline", fileName);
        return new ResponseEntity<>(new InputStreamResource(imageStream), headers, HttpStatus.OK);
    }

    // Lấy sản phẩm theo ID danh mục
    @Autowired
    private ProductService productService;

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






//  Lấy sản phẩm có khuyến mãi đang hoạt động
    @GetMapping("/with-promotions")
public ResponseEntity<List<Product>> getProductsWithPromotions() {
    List<Product> products = productService.getProductsWithActivePromotions();
    return ResponseEntity.ok(products);
}

}