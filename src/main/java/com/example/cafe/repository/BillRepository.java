package com.example.cafe.repository;

import com.example.cafe.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // ============ CŨ: giữ nguyên ============
    @Query("SELECT b FROM Bill b WHERE b.order.id = :orderId")
    Optional<Bill> findByOrderId(@Param("orderId") Long orderId);

    List<Bill> findByPaymentStatus(String paymentStatus);

    List<Bill> findByPaymentMethod(String paymentMethod);

    // ============ MỚI 1: phân trang + filter ============
    /**
     * Filter:
     * - keyword       : tìm theo ID hoá đơn hoặc ID đơn hàng
     * - paymentStatus : trạng thái (string)
     * - paymentMethod : phương thức (string)
     * - fromDate      : createdAt >= fromDate
     * - toDate        : createdAt <= toDate
     */
    @Query("""
            SELECT b FROM Bill b
            LEFT JOIN b.order o
            WHERE (:keyword IS NULL
                    OR CAST(b.id AS string) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%'))
              AND (:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus)
              AND (:paymentMethod IS NULL OR b.paymentMethod = :paymentMethod)
              AND (:fromDate IS NULL OR b.createdAt >= :fromDate)
              AND (:toDate IS NULL OR b.createdAt <= :toDate)
            """)
    Page<Bill> searchBills(
            @Param("keyword") String keyword,
            @Param("paymentStatus") String paymentStatus,
            @Param("paymentMethod") String paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    // ============ MỚI 2: thống kê toàn bộ ============
    /**
     * ⚠️ Vì paymentStatus là String, dùng string literal.
     * Nếu giá trị lưu trong DB khác (VD: "PAID" hay "paid") → sửa cho khớp.
     */
    @Query("""
            SELECT new map(
                COUNT(b) as total,
                COALESCE(SUM(CASE WHEN b.paymentStatus = 'PAID' THEN 1 ELSE 0 END), 0) as paidCount,
                COALESCE(SUM(CASE WHEN b.paymentStatus = 'PENDING' THEN 1 ELSE 0 END), 0) as pendingCount,
                COALESCE(SUM(CASE WHEN b.paymentStatus = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelledCount,
                COALESCE(SUM(b.totalAmount), 0) as totalRevenue
            )
            FROM Bill b
            """)
    Map<String, Object> getBillStats();
}