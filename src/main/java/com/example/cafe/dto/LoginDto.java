package com.example.cafe.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoginDto {
    private String username;
    private String password;
    private String portal;   
}