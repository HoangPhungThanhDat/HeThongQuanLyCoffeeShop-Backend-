package com.example.cafe.security.services.impl;

import com.example.cafe.entity.Category;
import com.example.cafe.entity.Product;
import com.example.cafe.repository.CategoryRepository;
import com.example.cafe.repository.ProductRepository;
import com.example.cafe.security.services.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;

    @Value("${project.image}")
    private String uploadDir;

    public ProductServiceImpl(ProductRepository repo, CategoryRepository categoryRepo) {
        this.repo = repo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public Product save(Product p) {
        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category cat = categoryRepo.findById(p.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategory(cat);
        }
        return repo.save(p);
    }

    @Override
    public Product update(Long id, Product p) {
        return repo.findById(id).map(existing -> {
            existing.setName(p.getName());
            existing.setDescription(p.getDescription());
            existing.setPrice(p.getPrice());
            existing.setStockQuantity(p.getStockQuantity());
            existing.setIsActive(p.getIsActive());

            if (p.getImageUrl() != null) {
                existing.setImageUrl(p.getImageUrl());
            }

            if (p.getCategory() != null && p.getCategory().getId() != null) {
                Category cat = categoryRepo.findById(p.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                existing.setCategory(cat);
            }
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return repo.findAll();
    }

    @Override
    public String saveImage(MultipartFile file) {
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Files.copy(file.getInputStream(),
                    Paths.get(uploadDir + File.separator + fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file ảnh: " + e.getMessage());
        }

        // Chỉ trả về tên file
        return fileName;
    }

    @Override
    public InputStream getImage(String fileName) throws FileNotFoundException {
        String fullPath = uploadDir + File.separator + fileName;
        return new FileInputStream(fullPath);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return repo.findByCategoryId(categoryId);
    }

    // ✅ Hàm mới: lấy 6 sản phẩm mới nhất
    public List<Product> getNewestProducts() {
        return repo.findNewestProducts();
    }

    @Override
    public Product reduceStock(Long id, int qty) {
        return repo.findById(id).map(existing -> {
            int newStock = existing.getStockQuantity() - qty;
            if (newStock < 0) {
                throw new RuntimeException("Không đủ tồn kho cho sản phẩm: " + existing.getName());
            }
            existing.setStockQuantity(newStock);
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Product không tồn tại"));
    }



    @Override
    public List<Product> getProductsWithActivePromotions() {
        return repo.findProductsWithActivePromotions();
    }

}
