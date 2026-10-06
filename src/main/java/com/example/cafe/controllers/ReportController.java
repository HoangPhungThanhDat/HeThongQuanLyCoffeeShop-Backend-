package com.example.cafe.controllers;

import com.example.cafe.dto.RevenueReportDTO;
import com.example.cafe.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/revenue?from=2026-10-01&to=2026-10-06
     * 
     * Hoặc không truyền gì → mặc định 30 ngày gần nhất
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDTO> getRevenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Default: 30 ngày gần nhất
        LocalDate toDate = (to != null) ? to : LocalDate.now();
        LocalDate fromDate = (from != null) ? from : toDate.minusDays(29);

        LocalDateTime fromDT = fromDate.atStartOfDay();
        LocalDateTime toDT = toDate.atTime(LocalTime.MAX);

        RevenueReportDTO report = reportService.getRevenueReport(fromDT, toDT);
        return ResponseEntity.ok(report);
    }
}