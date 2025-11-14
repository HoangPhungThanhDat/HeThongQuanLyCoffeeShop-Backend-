// package com.example.cafe.repository;

// import com.example.cafe.entity.Bill;
// import org.springframework.data.jpa.repository.JpaRepository;

// public interface BillRepository extends JpaRepository<Bill, Long> {
// }
















package com.example.cafe.repository;

import com.example.cafe.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    
    /**
     * ✅ Tìm Bill theo Order ID (QUAN TRỌNG!)
     *  * @param orderId ID của đơn hàng
     * @return Optional<Bill>
     */
    @Query("SELECT b FROM Bill b WHERE b.order.id = :orderId")
    Optional<Bill> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Tìm Bill theo payment_status
     */
    List<Bill> findByPaymentStatus(String paymentStatus);
    
    /**
     * Tìm Bill theo payment_method
     */
    List<Bill> findByPaymentMethod(String paymentMethod);
}