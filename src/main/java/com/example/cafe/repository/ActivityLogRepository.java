package com.example.cafe.repository;

import com.example.cafe.entity.ActivityLog;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>,
        JpaSpecificationExecutor<ActivityLog> {

    // ==================== COUNT ====================

    long countByLevel(LogLevel level);

    long countByCreatedAtAfter(LocalDateTime time);

    long countByLevelAndCreatedAtAfter(LogLevel level, LocalDateTime time);

    @Query("SELECT COUNT(DISTINCT a.username) FROM ActivityLog a WHERE a.username IS NOT NULL")
    long countDistinctUsers();

    // ==================== CHARTS ====================

    @Query(value = """
        SELECT HOUR(created_at) as hour, COUNT(*) as total
        FROM activity_logs
        WHERE created_at >= :fromTime
        GROUP BY HOUR(created_at)
        ORDER BY hour
        """, nativeQuery = true)
    List<Object[]> countByHourSince(@Param("fromTime") LocalDateTime fromTime);

    @Query(value = """
        SELECT DATE(created_at) as day,
               COUNT(*) as total,
               SUM(CASE WHEN level = 'ERROR' THEN 1 ELSE 0 END) as errors
        FROM activity_logs
        WHERE created_at >= :fromDate
        GROUP BY DATE(created_at)
        ORDER BY day
        """, nativeQuery = true)
    List<Object[]> countByDaySince(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT a.level, COUNT(a) FROM ActivityLog a GROUP BY a.level")
    List<Object[]> countGroupByLevel();

    @Query("SELECT a.action, COUNT(a) FROM ActivityLog a GROUP BY a.action")
    List<Object[]> countGroupByAction();

    // ==================== CLEANUP ====================

    /**
     * ✅ Xóa log cũ hơn cutoff — dùng cho cleanup job
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityLog a WHERE a.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    // ==================== DEDUPE (CHỐNG SPAM) ====================

    /**
     * ✅ Check log trùng — dùng cho chống spam
     * Trùng khi: cùng action + username + target + targetName, trong vòng N giây qua
     */
    boolean existsByActionAndUsernameAndTargetAndTargetNameAndCreatedAtAfter(
            LogAction action,
            String username,
            String target,
            String targetName,
            LocalDateTime since
    );
}