// package com.example.cafe.security.services.impl;

// import com.example.cafe.dto.PageResponse;
// import com.example.cafe.entity.Category;
// import com.example.cafe.entity.Product;
// import com.example.cafe.repository.CategoryRepository;
// import com.example.cafe.repository.ProductRepository;
// import com.example.cafe.security.services.ProductService;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.*;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;

// @Service
// public class ProductServiceImpl implements ProductService {

//     private final ProductRepository repo;
//     private final CategoryRepository categoryRepo;

//     @Value("${project.image}")
//     private String uploadDir;

//     public ProductServiceImpl(ProductRepository repo, CategoryRepository categoryRepo) {
//         this.repo = repo;
//         this.categoryRepo = categoryRepo;
//     }

//     @Override
//     public Product save(Product p) {
//         if (p.getCategory() != null && p.getCategory().getId() != null) {
//             Category cat = categoryRepo.findById(p.getCategory().getId())
//                     .orElseThrow(() -> new RuntimeException("Category not found"));
//             p.setCategory(cat);
//         }
//         return repo.save(p);
//     }

//     @Override
//     public Product update(Long id, Product p) {
//         return repo.findById(id).map(existing -> {
//             existing.setName(p.getName());
//             existing.setDescription(p.getDescription());
//             existing.setPrice(p.getPrice());
//             existing.setStockQuantity(p.getStockQuantity());
//             existing.setIsActive(p.getIsActive());

//             if (p.getImageUrl() != null) {
//                 existing.setImageUrl(p.getImageUrl());
//             }
//             if (p.getCategory() != null && p.getCategory().getId() != null) {
//                 Category cat = categoryRepo.findById(p.getCategory().getId())
//                         .orElseThrow(() -> new RuntimeException("Category not found"));
//                 existing.setCategory(cat);
//             }
//             return repo.save(existing);
//         }).orElseThrow(() -> new RuntimeException("Product not found"));
//     }

//     @Override
//     public void delete(Long id) {
//         repo.deleteById(id);
//     }

//     @Override
//     public Optional<Product> findById(Long id) {
//         return repo.findById(id);
//     }

//     @Override
//     public List<Product> findAll() {
//         return repo.findAll();
//     }

//     @Override
//     public String saveImage(MultipartFile file) {
//         File dir = new File(uploadDir);
//         if (!dir.exists()) dir.mkdirs();

//         String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
//         String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//         String fileName = UUID.randomUUID().toString() + fileExtension;

//         try {
//             Files.copy(file.getInputStream(),
//                     Paths.get(uploadDir + File.separator + fileName),
//                     StandardCopyOption.REPLACE_EXISTING);
//         } catch (IOException e) {
//             throw new RuntimeException("Lỗi lưu file ảnh: " + e.getMessage());
//         }
//         return fileName;
//     }

//     @Override
//     public InputStream getImage(String fileName) throws FileNotFoundException {
//         String fullPath = uploadDir + File.separator + fileName;
//         return new FileInputStream(fullPath);
//     }

//     @Override
//     public List<Product> getProductsByCategory(Long categoryId) {
//         return repo.findByCategoryId(categoryId);
//     }

//     @Override
//     public List<Product> getNewestProducts() {
//         return repo.findNewestProducts();
//     }

//     @Override
//     public Product reduceStock(Long id, int qty) {
//         return repo.findById(id).map(existing -> {
//             int newStock = existing.getStockQuantity() - qty;
//             if (newStock < 0) {
//                 throw new RuntimeException("Không đủ tồn kho cho sản phẩm: " + existing.getName());
//             }
//             existing.setStockQuantity(newStock);
//             return repo.save(existing);
//         }).orElseThrow(() -> new RuntimeException("Product không tồn tại"));
//     }

//     @Override
//     public List<Product> getProductsWithActivePromotions() {
//         return repo.findProductsWithActivePromotions();
//     }

//     // ===== MỚI 1: phân trang =====
//     @Override
//     public PageResponse<Product> getProductsPaged(String keyword, Long categoryId, Pageable pageable) {
//         String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
//         Page<Product> page = repo.searchProducts(kw, categoryId, pageable);
//         return PageResponse.from(page, p -> p);
//     }

//     // ===== MỚI 2: thống kê =====
//     @Override
//     public Map<String, Object> getProductStats() {
//         return repo.getProductStats();
//     }

//     // ===== MỚI 3: cho Menu — lấy toàn bộ sản phẩm đang bán =====
//     @Override
//     public List<Product> findAllActive() {
//         return repo.findByIsActiveTrueOrderByIdDesc();
//     }
// }
















package com.example.cafe.security.services.impl;

import com.example.cafe.dto.PageResponse;
import com.example.cafe.entity.Category;
import com.example.cafe.entity.Product;
import com.example.cafe.repository.CategoryRepository;
import com.example.cafe.repository.ProductRepository;
import com.example.cafe.security.services.ProductService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    @Value("${project.image}")
    private String uploadDir;

    public ProductServiceImpl(ProductRepository repo,
                              CategoryRepository categoryRepo,
                              ActivityLogService activityLogService) {   // 👈 MỚI
        this.repo = repo;
        this.categoryRepo = categoryRepo;
        this.activityLogService = activityLogService;
    }

    // ==================== SAVE (CREATE) ====================
    @Override
    public Product save(Product p) {
        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category cat = categoryRepo.findById(p.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategory(cat);
        }

        Product saved = repo.save(p);

        // ✅ GHI LOG
        try {
            activityLogService.logCreate(
                    "Sản phẩm",
                    saved.getName(),
                    "Thêm sản phẩm mới",
                    String.format("Tạo sản phẩm '%s' — giá %,.0fđ, tồn kho %d, danh mục %s",
                            saved.getName(),
                            saved.getPrice() != null ? saved.getPrice() : BigDecimal.ZERO,
                            saved.getStockQuantity() != null ? saved.getStockQuantity() : 0,
                            saved.getCategory() != null ? saved.getCategory().getName() : "N/A")
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE product: " + e.getMessage());
        }

        return saved;
    }

    // ==================== UPDATE ====================
    @Override
    public Product update(Long id, Product p) {
        Product old = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Lưu snapshot để log
        String oldName = old.getName();
        BigDecimal oldPrice = old.getPrice();
        Integer oldStock = old.getStockQuantity();

        Product updated = repo.findById(id).map(existing -> {
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

        // ✅ GHI LOG
        try {
            StringBuilder changes = new StringBuilder();
            if (oldName != null && !oldName.equals(updated.getName())) {
                changes.append("Tên: '").append(oldName).append("' → '").append(updated.getName()).append("'. ");
            }
            if (oldPrice != null && !oldPrice.equals(updated.getPrice())) {
                changes.append(String.format("Giá: %,.0fđ → %,.0fđ. ", oldPrice, updated.getPrice()));
            }
            if (oldStock != null && !oldStock.equals(updated.getStockQuantity())) {
                changes.append("Tồn kho: ").append(oldStock).append(" → ")
                        .append(updated.getStockQuantity()).append(". ");
            }

            activityLogService.logUpdate(
                    "Sản phẩm",
                    updated.getName(),
                    "Cập nhật sản phẩm",
                    changes.length() > 0
                            ? changes.toString().trim()
                            : "Cập nhật thông tin sản phẩm '" + updated.getName() + "'"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log UPDATE product: " + e.getMessage());
        }

        return updated;
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String name = p.getName();
        repo.deleteById(id);

        // ✅ GHI LOG
        try {
            activityLogService.logDelete(
                    "Sản phẩm",
                    name,
                    "Xóa sản phẩm",
                    "Đã xóa sản phẩm '" + name + "' khỏi hệ thống"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE product: " + e.getMessage());
        }
    }

    // ==================== CÁC METHOD CÒN LẠI (GIỮ NGUYÊN) ====================

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
        if (!dir.exists()) dir.mkdirs();

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

    @Override
    public List<Product> getNewestProducts() {
        return repo.findNewestProducts();
    }

    // ==================== REDUCE STOCK (có log khi cạn hàng) ====================
    @Override
    public Product reduceStock(Long id, int qty) {
        Product updated = repo.findById(id).map(existing -> {
            int newStock = existing.getStockQuantity() - qty;
            if (newStock < 0) {
                throw new RuntimeException("Không đủ tồn kho cho sản phẩm: " + existing.getName());
            }
            existing.setStockQuantity(newStock);
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Product không tồn tại"));

        // ✅ GHI LOG cảnh báo nếu tồn kho thấp (dưới 10)
        try {
            if (updated.getStockQuantity() != null && updated.getStockQuantity() < 10) {
                activityLogService.saveLog(
                        com.example.cafe.entity.enums.LogAction.UPDATE,
                        com.example.cafe.entity.enums.LogLevel.WARNING,
                        "Sản phẩm",
                        updated.getName(),
                        "Cảnh báo tồn kho thấp",
                        "Sản phẩm '" + updated.getName() + "' chỉ còn "
                                + updated.getStockQuantity() + " trong kho"
                );
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log reduce stock: " + e.getMessage());
        }

        return updated;
    }

    @Override
    public List<Product> getProductsWithActivePromotions() {
        return repo.findProductsWithActivePromotions();
    }

    @Override
    public PageResponse<Product> getProductsPaged(String keyword, Long categoryId, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> page = repo.searchProducts(kw, categoryId, pageable);
        return PageResponse.from(page, p -> p);
    }

    @Override
    public Map<String, Object> getProductStats() {
        return repo.getProductStats();
    }

    @Override
    public List<Product> findAllActive() {
        return repo.findByIsActiveTrueOrderByIdDesc();
    }
}