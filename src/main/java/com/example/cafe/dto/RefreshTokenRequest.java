package com.example.cafe.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken; // fallback nếu không dùng cookie
}