package com.example.cafe.services;

import com.example.cafe.dto.ActivityLogDTO;
import com.example.cafe.dto.LogFilterRequest;
import com.example.cafe.dto.LogStatsDTO;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ActivityLogService {

    // ==================== GHI LOG ====================

    /**
     * Ghi log chung — tự động lấy user từ SecurityContext
     */
    void saveLog(LogAction action, LogLevel level, String target,
                 String targetName, String description, String details);

    /**
     * Ghi log login/logout — set user thủ công (vì SecurityContext chưa có)
     */
    void logLoginByUsername(String username, boolean success);

    void logLogoutByUsername(String username);

    // Shortcut methods
    void logCreate(String target, String targetName, String description, String details);
    void logUpdate(String target, String targetName, String description, String details);
    void logDelete(String target, String targetName, String description, String details);

    // ==================== QUERY ====================

    Page<ActivityLogDTO> getLogs(LogFilterRequest filter);
    ActivityLogDTO getById(Long id);
    LogStatsDTO getStats();

    // ==================== CHARTS ====================

    List<Map<String, Object>> getDailyChart(int days);
    List<Map<String, Object>> getHourlyChart(int hours);
    List<Map<String, Object>> getLevelDistribution();
    List<Map<String, Object>> getActionDistribution();
}