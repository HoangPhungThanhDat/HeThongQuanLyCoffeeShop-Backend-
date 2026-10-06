package com.example.cafe.services;

import com.example.cafe.dto.RevenueReportDTO;

import java.time.LocalDateTime;

public interface ReportService {
    RevenueReportDTO getRevenueReport(LocalDateTime from, LocalDateTime to);
}