package com.example.cafe.repository;

import java.util.List;
import com.example.cafe.entity.Order;
import com.example.cafe.entity.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // ✅ Tìm Order theo status
    List<Order> findByStatus(OrderStatus status);
    
    // ✅ Tìm Order theo table ID
    List<Order> findByTableId(Long tableId);
    
    // ✅ Tìm Order theo employee ID
    List<Order> findByEmployeeId(Long employeeId);
}