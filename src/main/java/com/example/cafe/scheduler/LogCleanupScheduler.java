package com.example.cafe.scheduler;

import com.example.cafe.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ✅ Scheduled job tự động dọn log cũ
 * Chạy mỗi ngày lúc 2h sáng (cron: 0 0 2 * * ?)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogCleanupScheduler {

    private final ActivityLogRepository logRepository;

    // Số ngày giữ log — config trong application.properties, mặc định 90
    @Value("${log.retention-days:90}")
    private int retentionDays;

    /**
     * Cron: giây phút giờ ngày tháng thứ
     * "0 0 2 * * ?" = 2:00:00 AM mỗi ngày
     *
     * Ví dụ khác:
     * - "0 0 * * * ?"     → mỗi giờ (phút 0)
     * - "0 0 2 * * MON"   → 2h sáng thứ 2
     * - "0 0 2 1 * ?"     → 2h sáng ngày 1 mỗi tháng
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
            int deleted = logRepository.deleteByCreatedAtBefore(cutoff);

            if (deleted > 0) {
                log.info("🧹 [LogCleanup] Đã xóa {} log cũ hơn {} ngày (trước {})",
                        deleted, retentionDays, cutoff);
            } else {
                log.info("🧹 [LogCleanup] Không có log nào cần xóa (giữ {} ngày)", retentionDays);
            }
        } catch (Exception e) {
            log.error("❌ [LogCleanup] Lỗi khi dọn log: {}", e.getMessage(), e);
        }
    }
}