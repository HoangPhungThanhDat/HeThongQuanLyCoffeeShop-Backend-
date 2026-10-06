package com.example.cafe.repository;

import java.util.List;
import java.util.Map;

import com.example.cafe.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ===== CŨ: giữ nguyên =====
    List<Product> findByCategoryId(Long categoryId);

    @Query(value = "SELECT * FROM products WHERE is_active = 1 ORDER BY id DESC LIMIT 6", nativeQuery = true)
    List<Product> findNewestProducts();

    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.promotions promo
            WHERE promo.isActive = true
              AND (promo.startDate IS NULL OR promo.startDate <= CURRENT_DATE)
              AND (promo.endDate IS NULL OR promo.endDate >= CURRENT_DATE)
            """)
    List<Product> findProductsWithActivePromotions();

    // ===== MỚI 1: phân trang + filter =====
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // ===== MỚI 2: thống kê =====
    @Query("""
            SELECT new map(
                COUNT(p) as total,
                COALESCE(SUM(CASE WHEN p.isActive = true THEN 1 ELSE 0 END), 0) as active,
                COALESCE(SUM(CASE WHEN p.isActive = false THEN 1 ELSE 0 END), 0) as inactive,
                COALESCE(SUM(CASE WHEN p.stockQuantity <= 10 THEN 1 ELSE 0 END), 0) as lowStock
            )
            FROM Product p
            """)
    Map<String, Object> getProductStats();

    // ===== MỚI 3: Lấy toàn bộ sản phẩm active cho Menu (không phân trang) =====
    List<Product> findByIsActiveTrueOrderByIdDesc();
}