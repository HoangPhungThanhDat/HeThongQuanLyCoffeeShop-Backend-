package com.example.cafe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.cafe.entity.Order;
import com.example.cafe.entity.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ============ CŨ: giữ nguyên ============
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByTableId(Long tableId);
    List<Order> findByEmployeeId(Long employeeId);

    // ============ MỚI 1: phân trang + filter ============
    /**
     * Filter theo:
     * - keyword  : tìm theo tên khách hàng (employee) hoặc SĐT khách
     * - status   : trạng thái đơn (OrderStatus enum)
     * - fromDate : từ ngày (createdAt)
     * - toDate   : đến ngày (createdAt)
     * - tableId  : theo bàn
     *
     * ⚠️ Lưu ý: Order không có "orderCode" theo code bạn gửi.
     *    Tôi dùng "id" (cast sang string) để tìm theo mã đơn.
     *    Nếu entity Order có field "customerName" hoặc "customerPhone" → thêm vào.
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN o.employee e
            LEFT JOIN o.table t
            WHERE (:keyword IS NULL
                    OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR o.status = :status)
              AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
              AND (:toDate IS NULL OR o.createdAt <= :toDate)
              AND (:tableId IS NULL OR t.id = :tableId)
            """)
    Page<Order> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("tableId") Long tableId,
            Pageable pageable
    );

    // ============ MỚI 2: thống kê toàn bộ ============
    /**
     * ⚠️ So sánh enum trong JPQL:
     * - Nếu OrderStatus là enum chuẩn Java → dùng o.status = com.example.cafe.entity.enums.OrderStatus.PENDING
     * - Hoặc dùng tham số enum (khuyên dùng) → xem cách viết bên dưới
     *
     * Cách an toàn nhất: dùng native query hoặc truyền enum làm tham số.
     * Dưới đây dùng cách cast enum thành string để tránh lỗi.
     */
    @Query("""
            SELECT new map(
                COUNT(o) as total,
                COALESCE(SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END), 0) as pending,
                COALESCE(SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) as confirmed,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN 1 ELSE 0 END), 0) as paid,
                COALESCE(SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) as cancelled,
                COALESCE(SUM(o.totalAmount), 0) as totalRevenue
            )
            FROM Order o
            """)
    Map<String, Object> getOrderStats();
}