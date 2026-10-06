package com.example.cafe.controllers;

import com.example.cafe.dto.ActivityLogDTO;
import com.example.cafe.dto.LogFilterRequest;
import com.example.cafe.dto.LogStatsDTO;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import com.example.cafe.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityLogController {

    private final ActivityLogService logService;

    // GET /api/logs?action=&level=&timeRange=&keyword=&page=&size=
    @GetMapping
    public ResponseEntity<Page<ActivityLogDTO>> getLogs(
            @RequestParam(required = false) LogAction action,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        LogFilterRequest filter = LogFilterRequest.builder()
                .action(action)
                .level(level)
                .timeRange(timeRange)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(logService.getLogs(filter));
    }

    // GET /api/logs/stats
    @GetMapping("/stats")
    public ResponseEntity<LogStatsDTO> getStats() {
        return ResponseEntity.ok(logService.getStats());
    }

    // GET /api/logs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ActivityLogDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getById(id));
    }

    // GET /api/logs/charts/daily?days=7
    @GetMapping("/charts/daily")
    public ResponseEntity<List<Map<String, Object>>> daily(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(logService.getDailyChart(days));
    }

    // GET /api/logs/charts/hourly?hours=24
    @GetMapping("/charts/hourly")
    public ResponseEntity<List<Map<String, Object>>> hourly(@RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(logService.getHourlyChart(hours));
    }

    // GET /api/logs/charts/level-distribution
    @GetMapping("/charts/level-distribution")
    public ResponseEntity<List<Map<String, Object>>> levelDist() {
        return ResponseEntity.ok(logService.getLevelDistribution());
    }

    // GET /api/logs/charts/action-distribution
    @GetMapping("/charts/action-distribution")
    public ResponseEntity<List<Map<String, Object>>> actionDist() {
        return ResponseEntity.ok(logService.getActionDistribution());
    }
}