package com.example.cafe.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodStatDTO {
    private String method;   // CASH / CARD / MOBILE
    private BigDecimal revenue;
    private Long count;
}