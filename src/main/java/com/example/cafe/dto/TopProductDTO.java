package com.example.cafe.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductDTO {
    private Long productId;
    private String name;
    private String imageUrl;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}