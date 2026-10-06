package com.example.cafe.services.impl;

import com.example.cafe.dto.*;
import com.example.cafe.repository.ReportRepository;
import com.example.cafe.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;

    @Override
    public RevenueReportDTO getRevenueReport(LocalDateTime from, LocalDateTime to) {
        // ===== SUMMARY =====
        Object[] summary = reportRepo.getSummary(from, to);
        BigDecimal totalRevenue = summary[0] != null
                ? new BigDecimal(summary[0].toString()) : BigDecimal.ZERO;
        Long totalOrders = summary[1] != null
                ? ((Number) summary[1]).longValue() : 0L;
        BigDecimal avgOrder = summary[2] != null
                ? new BigDecimal(summary[2].toString()) : BigDecimal.ZERO;

        Long uniqueCustomers = reportRepo.countUniqueOrders(from, to);

        // ===== SO SÁNH KỲ TRƯỚC =====
        long daysBetween = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;
        LocalDateTime prevFrom = from.minusDays(daysBetween);
        LocalDateTime prevTo = from.minusSeconds(1);

        Object[] prevSummary = reportRepo.getSummary(prevFrom, prevTo);
        BigDecimal previousRevenue = prevSummary[0] != null
                ? new BigDecimal(prevSummary[0].toString()) : BigDecimal.ZERO;

        Double growthPercent = 0.0;
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercent = totalRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // ===== DAILY =====
        List<DailyRevenueDTO> dailyRevenue = reportRepo.getDailyRevenue(from, to);

        // ===== PAYMENT METHODS =====
        List<PaymentMethodStatDTO> paymentStats = reportRepo.getPaymentMethodStats(from, to);

        // ===== TOP PRODUCTS =====
        List<TopProductDTO> topProducts = reportRepo.getTopProducts(from, to, 10);

        return RevenueReportDTO.builder()
                .fromDate(from)
                .toDate(to)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .avgOrderValue(avgOrder)
                .uniqueCustomers(uniqueCustomers)
                .previousRevenue(previousRevenue)
                .growthPercent(growthPercent)
                .dailyRevenue(dailyRevenue)
                .paymentMethodStats(paymentStats)
                .topProducts(topProducts)
                .build();
    }
}