package com.example.cafe.dto;

import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogFilterRequest {
    private LogAction action;
    private LogLevel level;
    private String timeRange;   // "today" | "week" | "month" | null
    private String keyword;
    private Integer page = 0;
    private Integer size = 20;
}