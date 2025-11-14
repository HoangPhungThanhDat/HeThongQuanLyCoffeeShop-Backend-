package com.example.cafe.repository;

import java.util.List;
import com.example.cafe.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
        // Lấy sản phẩm theo ID danh mục
        List<Product> findByCategoryId(Long categoryId);

        @Query(value = "SELECT * FROM products WHERE is_active = 1 ORDER BY id DESC LIMIT 6", nativeQuery = true)
        List<Product> findNewestProducts();

        // Lấy sản phẩm có khuyến mãi đang hoạt động
        @Query("""
                            SELECT DISTINCT p FROM Product p
                            JOIN p.promotions promo
                            WHERE promo.isActive = true
                              AND (promo.startDate IS NULL OR promo.startDate <= CURRENT_DATE)
                              AND (promo.endDate IS NULL OR promo.endDate >= CURRENT_DATE)
                        """)
        List<Product> findProductsWithActivePromotions();
}
