package com.example.cafe.dto;

import com.example.cafe.entity.enums.PaymentMethod;
import com.example.cafe.entity.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private Long id;
    private Long orderId;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;  
    private PaymentStatus paymentStatus;  
    private String notes;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
