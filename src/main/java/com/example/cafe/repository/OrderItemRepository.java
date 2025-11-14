package com.example.cafe.repository;

import java.util.List;
import com.example.cafe.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Tìm tất cả OrderItem của một Order
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Tìm OrderItem theo Order và Product
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.product.id = :productId")
    List<OrderItem> findByOrderIdAndProductId(
        @Param("orderId") Long orderId, 
        @Param("productId") Long productId
    );
    
    /**
     * Xóa tất cả OrderItem của một Order
     */
    void deleteByOrderId(Long orderId);
}