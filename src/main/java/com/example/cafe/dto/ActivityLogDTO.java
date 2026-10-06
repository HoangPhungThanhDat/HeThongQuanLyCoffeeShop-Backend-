package com.example.cafe.dto;

import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogDTO {
    private Long id;
    private LogAction action;
    private LogLevel level;
    private Long userId;
    private String username;
    private String fullName;
    private String avatar;
    private String role;
    private String target;
    private String targetName;
    private String description;
    private String details;
    private String ipAddress;
    private String device;
    private LocalDateTime createdAt;

    // Nested user object để FE dùng trực tiếp (khớp MOCK_LOGS)
    public UserInfo getUser() {
        return new UserInfo(fullName, username, avatar, role);
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private String fullName;
        private String username;
        private String avatar;
        private String role;
    }
}