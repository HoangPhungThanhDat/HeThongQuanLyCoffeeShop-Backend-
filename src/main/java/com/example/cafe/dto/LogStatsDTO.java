package com.example.cafe.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogStatsDTO {
    private long total;
    private long today;
    private long success;
    private long info;
    private long warnings;
    private long errors;
    private long uniqueUsers;
}