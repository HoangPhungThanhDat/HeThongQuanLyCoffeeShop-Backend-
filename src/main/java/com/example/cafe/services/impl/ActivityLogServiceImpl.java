package com.example.cafe.services.impl;

import com.example.cafe.dto.ActivityLogDTO;
import com.example.cafe.dto.LogFilterRequest;
import com.example.cafe.dto.LogStatsDTO;
import com.example.cafe.entity.ActivityLog;
import com.example.cafe.entity.User;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import com.example.cafe.repository.ActivityLogRepository;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.services.ActivityLogService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final UserRepository userRepository;

    // ✅ Số giây chống spam — 60s = 1 phút
    private static final int DEDUPE_WINDOW_SECONDS = 60;

    // ==================== GHI LOG ====================

    @Override
    @Transactional
    public void saveLog(LogAction action, LogLevel level, String target,
                        String targetName, String description, String details) {
        try {
            // ✅ B1: Lấy username từ SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = extractUsernameFromAuth(auth);
            if (username != null) {
                username = truncate(username, 50);
            }

            // ✅ B2: Check dedupe — nếu log trùng trong 60s qua → BỎ QUA
            if (isDuplicate(action, level, username, target, targetName)) {
                log.debug("⏭️ Skip duplicate log: {} - {} - {} - {}",
                        action, username, target, targetName);
                return;
            }

            // ✅ B3: Tạo log
            ActivityLog.ActivityLogBuilder builder = ActivityLog.builder()
                    .action(action)
                    .level(level)
                    .target(target)
                    .targetName(targetName)
                    .description(description)
                    .details(details)
                    .ipAddress(extractIp())
                    .device(extractDevice())
                    .createdAt(LocalDateTime.now());

            if (username != null && !username.isBlank()) {
                builder.username(username);

                final String finalUsername = username;
                userRepository.findByUsername(finalUsername).ifPresent(u -> {
                    builder.userId(u.getId());
                    builder.fullName(u.getFullName());
                    builder.role(u.getRole() != null ? u.getRole().name() : null);
                    builder.avatar(buildAvatar(u.getFullName()));
                });
            }

            logRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logLoginByUsername(String username, boolean success) {
        try {
            if (username != null) username = truncate(username, 50);

            // ✅ Dedupe cho LOGIN SUCCESS (không dedupe LOGIN fail)
            if (success && isDuplicate(LogAction.LOGIN, LogLevel.SUCCESS,
                    username, "Hệ thống", "Đăng nhập")) {
                log.debug("⏭️ Skip duplicate LOGIN success for {}", username);
                return;
            }

            ActivityLog.ActivityLogBuilder builder = ActivityLog.builder()
                    .action(LogAction.LOGIN)
                    .level(success ? LogLevel.SUCCESS : LogLevel.ERROR)
                    .target("Hệ thống")
                    .targetName(success ? "Đăng nhập" : "Đăng nhập thất bại")
                    .description(success ? "Đăng nhập thành công" : "Đăng nhập thất bại")
                    .details(success
                            ? "Người dùng " + username + " đăng nhập vào hệ thống"
                            : "Sai tài khoản hoặc mật khẩu: " + username)
                    .ipAddress(extractIp())
                    .device(extractDevice())
                    .username(username)
                    .createdAt(LocalDateTime.now());

            final String finalUsername = username;
            if (finalUsername != null) {
                userRepository.findByUsername(finalUsername).ifPresent(u -> {
                    builder.userId(u.getId());
                    builder.fullName(u.getFullName());
                    builder.role(u.getRole() != null ? u.getRole().name() : null);
                    builder.avatar(buildAvatar(u.getFullName()));
                });
            }

            ActivityLog logToSave = builder.build();
            if (logToSave.getAvatar() == null) {
                logToSave.setAvatar("?");
            }

            logRepository.save(logToSave);
        } catch (Exception e) {
            log.error("Failed to save login log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logLogoutByUsername(String username) {
        try {
            if (username != null) username = truncate(username, 50);

            // ✅ Dedupe cho LOGOUT
            if (isDuplicate(LogAction.LOGOUT, LogLevel.INFO,
                    username, "Hệ thống", "Đăng xuất")) {
                log.debug("⏭️ Skip duplicate LOGOUT for {}", username);
                return;
            }

            ActivityLog.ActivityLogBuilder builder = ActivityLog.builder()
                    .action(LogAction.LOGOUT)
                    .level(LogLevel.INFO)
                    .target("Hệ thống")
                    .targetName("Đăng xuất")
                    .description("Đăng xuất khỏi hệ thống")
                    .details("Người dùng " + username + " đã đăng xuất")
                    .ipAddress(extractIp())
                    .device(extractDevice())
                    .username(username)
                    .createdAt(LocalDateTime.now());

            final String finalUsername = username;
            if (finalUsername != null) {
                userRepository.findByUsername(finalUsername).ifPresent(u -> {
                    builder.userId(u.getId());
                    builder.fullName(u.getFullName());
                    builder.role(u.getRole() != null ? u.getRole().name() : null);
                    builder.avatar(buildAvatar(u.getFullName()));
                });
            }

            logRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Failed to save logout log: {}", e.getMessage(), e);
        }
    }

    @Override
    public void logCreate(String target, String targetName, String description, String details) {
        saveLog(LogAction.CREATE, LogLevel.SUCCESS, target, targetName, description, details);
    }

    @Override
    public void logUpdate(String target, String targetName, String description, String details) {
        saveLog(LogAction.UPDATE, LogLevel.INFO, target, targetName, description, details);
    }

    @Override
    public void logDelete(String target, String targetName, String description, String details) {
        saveLog(LogAction.DELETE, LogLevel.WARNING, target, targetName, description, details);
    }

    // ==================== QUERY ====================

    @Override
    public Page<ActivityLogDTO> getLogs(LogFilterRequest filter) {
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                filter.getSize() != null ? filter.getSize() : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<ActivityLog> spec = buildSpec(filter);
        return logRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    public ActivityLogDTO getById(Long id) {
        ActivityLog entity = logRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy log id=" + id));
        return toDTO(entity);
    }

    @Override
    public LogStatsDTO getStats() {
        long total = logRepository.count();
        long today = logRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());
        long success = logRepository.countByLevel(LogLevel.SUCCESS);
        long info = logRepository.countByLevel(LogLevel.INFO);
        long warning = logRepository.countByLevel(LogLevel.WARNING);
        long error = logRepository.countByLevel(LogLevel.ERROR);
        long users = logRepository.countDistinctUsers();

        return LogStatsDTO.builder()
                .total(total)
                .today(today)
                .success(success)
                .info(info)
                .warnings(warning)
                .errors(error)
                .uniqueUsers(users)
                .build();
    }

    // ==================== CHARTS ====================

    @Override
    public List<Map<String, Object>> getDailyChart(int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> rows = logRepository.countByDaySince(from);
        Map<String, Map<String, Object>> byDate = new HashMap<>();

        for (Object[] r : rows) {
            String day = r[0].toString();
            long total = ((Number) r[1]).longValue();
            long errors = ((Number) r[2]).longValue();
            byDate.put(day, Map.of("day", day, "total", total, "errors", errors));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String key = LocalDate.now().minusDays(i).toString();
            result.add(byDate.getOrDefault(key, Map.of("day", key, "total", 0L, "errors", 0L)));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getHourlyChart(int hours) {
        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        List<Object[]> rows = logRepository.countByHourSince(from);
        Map<Integer, Long> byHour = new HashMap<>();
        for (Object[] r : rows) {
            byHour.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = hours - 1; i >= 0; i--) {
            int h = LocalDateTime.now().minusHours(i).getHour();
            result.add(Map.of("hour", h, "logs", byHour.getOrDefault(h, 0L)));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getLevelDistribution() {
        List<Object[]> rows = logRepository.countGroupByLevel();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(Map.of(
                    "name", r[0].toString(),
                    "value", ((Number) r[1]).longValue()
            ));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getActionDistribution() {
        List<Object[]> rows = logRepository.countGroupByAction();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(Map.of(
                    "name", r[0].toString(),
                    "value", ((Number) r[1]).longValue()
            ));
        }
        return result;
    }

    // ==================== HELPERS ====================

    /**
     * ✅ DEDUPE: Check xem log có bị trùng không
     * CHỈ dedupe: VIEW, LOGIN success, LOGOUT
     * KHÔNG dedupe: CREATE, UPDATE, DELETE, SECURITY, LOGIN fail
     */
    private boolean isDuplicate(LogAction action, LogLevel level, String username,
                                String target, String targetName) {
        // Không có username → không dedupe (không xác định ai → giữ hết)
        if (username == null || username.isBlank()) return false;

        // ✅ CHỈ dedupe 3 action spam-prone
        boolean shouldDedupe = (action == LogAction.VIEW
                || action == LogAction.LOGIN
                || action == LogAction.LOGOUT);
        if (!shouldDedupe) return false;

        // Không dedupe LOGIN fail (dấu hiệu brute force → giữ hết)
        if (action == LogAction.LOGIN && level == LogLevel.ERROR) return false;

        try {
            LocalDateTime since = LocalDateTime.now()
                    .minusSeconds(DEDUPE_WINDOW_SECONDS);
            return logRepository.existsByActionAndUsernameAndTargetAndTargetNameAndCreatedAtAfter(
                    action,
                    truncate(username, 50),
                    target,
                    targetName,
                    since
            );
        } catch (Exception e) {
            log.error("Error checking duplicate: {}", e.getMessage());
            return false;   // Lỗi → cho phép ghi log (an toàn hơn)
        }
    }

    /**
     * ✅ Extract username an toàn từ Authentication
     */
    private String extractUsernameFromAuth(Authentication auth) {
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal == null) return null;

        // Case 1: principal là String (username)
        if (principal instanceof String) {
            String s = (String) principal;
            if (!"anonymousUser".equals(s)) return s;
            return null;
        }

        // Case 2: principal là User entity
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }

        // Case 3: fallback dùng auth.getName()
        try {
            String name = auth.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                return name;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * ✅ Truncate string nếu dài hơn maxLength
     */
    private String truncate(String s, int maxLength) {
        if (s == null) return null;
        return s.length() > maxLength ? s.substring(0, maxLength) : s;
    }

    private Specification<ActivityLog> buildSpec(LogFilterRequest f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (f.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), f.getAction()));
            }
            if (f.getLevel() != null) {
                predicates.add(cb.equal(root.get("level"), f.getLevel()));
            }

            if (f.getTimeRange() != null) {
                LocalDateTime from = switch (f.getTimeRange()) {
                    case "today" -> LocalDate.now().atStartOfDay();
                    case "week" -> LocalDate.now().minusDays(7).atStartOfDay();
                    case "month" -> LocalDate.now().minusDays(30).atStartOfDay();
                    default -> null;
                };
                if (from != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                }
            }

            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                String kw = "%" + f.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), kw),
                        cb.like(cb.lower(root.get("fullName")), kw),
                        cb.like(cb.lower(root.get("targetName")), kw),
                        cb.like(cb.lower(root.get("description")), kw),
                        cb.like(cb.lower(root.get("ipAddress")), kw)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ActivityLogDTO toDTO(ActivityLog e) {
        return ActivityLogDTO.builder()
                .id(e.getId())
                .action(e.getAction())
                .level(e.getLevel())
                .userId(e.getUserId())
                .username(e.getUsername())
                .fullName(e.getFullName())
                .avatar(e.getAvatar())
                .role(e.getRole())
                .target(e.getTarget())
                .targetName(e.getTargetName())
                .description(e.getDescription())
                .details(e.getDetails())
                .ipAddress(e.getIpAddress())
                .device(e.getDevice())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private String buildAvatar(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        return String.valueOf(fullName.charAt(0)).toUpperCase();
    }

    private String extractIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
            if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractDevice() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }
}