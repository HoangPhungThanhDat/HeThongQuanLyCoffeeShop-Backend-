package com.example.cafe.controllers;

import com.example.cafe.entity.User;
import com.example.cafe.entity.enums.Role;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.JwtService;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

  @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE) // Mặc định là EMPLOYEE nếu không cung cấp
                .isActive(true)
                .build();

        userRepository.save(newUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registered successfully");

        return ResponseEntity.ok(response);
    }
    // 🔹 Đăng nhập
  @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtService.generateToken(user.getUsername());

        // Tạo response theo format mong muốn
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("roles", List.of(user.getRole().name())); 
        response.put("accessToken", token);

        return ResponseEntity.ok(response);
    }
}
