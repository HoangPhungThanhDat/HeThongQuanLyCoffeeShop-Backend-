package com.example.cafe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.cafe.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ============ CŨ: giữ nguyên ============
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.product.id = :productId")
    List<OrderItem> findByOrderIdAndProductId(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId
    );

    void deleteByOrderId(Long orderId);

    // ============ MỚI 1: phân trang + filter ============
    /**
     * Filter:
     * - keyword   : tìm theo tên sản phẩm
     * - orderId   : theo đơn hàng
     * - productId : theo sản phẩm
     * - fromDate  : createdAt >= fromDate
     * - toDate    : createdAt <= toDate
     */
    @Query("""
            SELECT oi FROM OrderItem oi
            LEFT JOIN oi.order o
            LEFT JOIN oi.product p
            WHERE (:keyword IS NULL
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:orderId IS NULL OR o.id = :orderId)
              AND (:productId IS NULL OR p.id = :productId)
              AND (:fromDate IS NULL OR oi.createdAt >= :fromDate)
              AND (:toDate IS NULL OR oi.createdAt <= :toDate)
            """)
    Page<OrderItem> searchOrderItems(
            @Param("keyword") String keyword,
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    // ============ MỚI 2: thống kê toàn bộ ============
    @Query("""
            SELECT new map(
                COUNT(oi) as total,
                COALESCE(SUM(oi.quantity), 0) as totalQuantity,
                COALESCE(SUM(oi.subtotal), 0) as totalRevenue,
                COALESCE(AVG(oi.price), 0) as avgPrice
            )
            FROM OrderItem oi
            """)
    Map<String, Object> getOrderItemStats();
}