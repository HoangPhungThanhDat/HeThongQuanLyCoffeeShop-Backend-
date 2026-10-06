package com.example.cafe.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
}