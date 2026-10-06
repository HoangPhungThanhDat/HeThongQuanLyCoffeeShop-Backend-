package com.example.cafe.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDTO {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orderCount;
}