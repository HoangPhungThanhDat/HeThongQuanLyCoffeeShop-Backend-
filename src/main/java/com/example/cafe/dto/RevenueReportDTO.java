package com.example.cafe.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDTO {

    // Range info
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Summary
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal avgOrderValue;
    private Long uniqueCustomers;

    // So sánh kỳ trước
    private BigDecimal previousRevenue;
    private Double growthPercent;

    // Charts
    private List<DailyRevenueDTO> dailyRevenue;
    private List<PaymentMethodStatDTO> paymentMethodStats;

    // Top products
    private List<TopProductDTO> topProducts;
}