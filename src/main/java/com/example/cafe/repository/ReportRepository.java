package com.example.cafe.repository;

import com.example.cafe.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Repository chuyên cho báo cáo — dùng Native SQL + EntityManager
 * Không extends JpaRepository vì cần aggregate queries phức tạp
 */
@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager em;

    // ==================== SUMMARY ====================

    /**
     * Tổng doanh thu + số đơn + AOV — CHỈ tính bill COMPLETED
     */
    public Object[] getSummary(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT 
                COALESCE(SUM(b.total_amount), 0) as total_revenue,
                COUNT(DISTINCT b.id) as total_orders,
                COALESCE(AVG(b.total_amount), 0) as avg_order_value
            FROM bills b
            WHERE b.payment_status = 'COMPLETED'
              AND b.created_at BETWEEN :from AND :to
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return (Object[]) query.getSingleResult();
    }

    /**
     * Đếm số khách hàng unique — dựa vào order_id
     */
    public Long countUniqueOrders(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT COUNT(DISTINCT b.order_id)
            FROM bills b
            WHERE b.payment_status = 'COMPLETED'
              AND b.created_at BETWEEN :from AND :to
            """;
        Query query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    // ==================== DAILY CHART ====================

    /**
     * Doanh thu theo ngày — group by DATE(created_at)
     */
    @SuppressWarnings("unchecked")
    public List<DailyRevenueDTO> getDailyRevenue(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT 
                DATE(b.created_at) as day,
                COALESCE(SUM(b.total_amount), 0) as revenue,
                COUNT(DISTINCT b.id) as order_count
            FROM bills b
            WHERE b.payment_status = 'COMPLETED'
              AND b.created_at BETWEEN :from AND :to
            GROUP BY DATE(b.created_at)
            ORDER BY day
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);

        List<Object[]> rows = query.getResultList();
        List<DailyRevenueDTO> result = new ArrayList<>();

        for (Object[] r : rows) {
            LocalDate day = null;
            if (r[0] instanceof java.sql.Date) {
                day = ((java.sql.Date) r[0]).toLocalDate();
            } else if (r[0] instanceof LocalDate) {
                day = (LocalDate) r[0];
            }

            BigDecimal revenue = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            Long count = r[2] != null ? ((Number) r[2]).longValue() : 0L;

            result.add(DailyRevenueDTO.builder()
                    .date(day)
                    .revenue(revenue)
                    .orderCount(count)
                    .build());
        }
        return result;
    }

    // ==================== PAYMENT METHODS ====================

    @SuppressWarnings("unchecked")
    public List<PaymentMethodStatDTO> getPaymentMethodStats(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT 
                b.payment_method,
                COALESCE(SUM(b.total_amount), 0) as revenue,
                COUNT(*) as count
            FROM bills b
            WHERE b.payment_status = 'COMPLETED'
              AND b.created_at BETWEEN :from AND :to
              AND b.payment_method IS NOT NULL
            GROUP BY b.payment_method
            ORDER BY revenue DESC
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);

        List<Object[]> rows = query.getResultList();
        List<PaymentMethodStatDTO> result = new ArrayList<>();

        for (Object[] r : rows) {
            String method = r[0] != null ? r[0].toString() : "UNKNOWN";
            BigDecimal revenue = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            Long count = r[2] != null ? ((Number) r[2]).longValue() : 0L;

            result.add(PaymentMethodStatDTO.builder()
                    .method(method)
                    .revenue(revenue)
                    .count(count)
                    .build());
        }
        return result;
    }

    // ==================== TOP PRODUCTS ====================

    @SuppressWarnings("unchecked")
    public List<TopProductDTO> getTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        String sql = """
            SELECT 
                oi.product_id,
                p.name,
                p.image_url,
                SUM(oi.quantity) as total_quantity,
                SUM(oi.subtotal) as total_revenue
            FROM order_items oi
            INNER JOIN products p ON oi.product_id = p.id
            INNER JOIN orders o ON oi.order_id = o.id
            INNER JOIN bills b ON b.order_id = o.id
            WHERE b.payment_status = 'COMPLETED'
              AND b.created_at BETWEEN :from AND :to
            GROUP BY oi.product_id, p.name, p.image_url
            ORDER BY total_revenue DESC
            LIMIT :lim
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setParameter("lim", limit);

        List<Object[]> rows = query.getResultList();
        List<TopProductDTO> result = new ArrayList<>();

        for (Object[] r : rows) {
            Long productId = r[0] != null ? ((Number) r[0]).longValue() : null;
            String name = r[1] != null ? r[1].toString() : "N/A";
            String imageUrl = r[2] != null ? r[2].toString() : null;
            Long quantity = r[3] != null ? ((Number) r[3]).longValue() : 0L;
            BigDecimal revenue = r[4] != null ? new BigDecimal(r[4].toString()) : BigDecimal.ZERO;

            result.add(TopProductDTO.builder()
                    .productId(productId)
                    .name(name)
                    .imageUrl(imageUrl)
                    .totalQuantity(quantity)
                    .totalRevenue(revenue)
                    .build());
        }
        return result;
    }
}