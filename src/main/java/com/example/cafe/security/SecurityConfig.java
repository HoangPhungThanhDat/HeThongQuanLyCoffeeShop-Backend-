package com.example.cafe.security;

import com.example.cafe.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // ============================================
                // PUBLIC: Payment gateways
                // ============================================
                .requestMatchers("/api/payment/**").permitAll()
                .requestMatchers("/api/momo/**").permitAll()

                // ============================================
                // PUBLIC: Auth + Image serving
                // ============================================
                .requestMatchers(
                    "/api/auth/**",
                    "/api/products/image/**",
                    "/api/users/image/**"
                ).permitAll()

                // ============================================
                // PUBLIC: GET (khách/staff xem menu, đơn, bàn)
                // ============================================
                .requestMatchers(HttpMethod.GET,
                    "/api/products/**",
                    "/api/products/category/**",
                    "/api/tables/**",
                    "/api/categories/**",
                    "/api/orders/**",
                    "/api/order-items/**",
                    "/api/bills/**",
                    "/api/promotions/**",
                    "/api/reports/**"
                ).permitAll()

                // ============================================
                // PUBLIC: POST cho khách đặt hàng / POS tạo đơn
                // ============================================
                .requestMatchers(HttpMethod.POST,
                    "/api/orders/**",
                    "/api/bills/**",
                    "/api/order-items/**"
                ).permitAll()

                // ============================================
                // LOGS: CHỈ ADMIN
                // ============================================
                .requestMatchers("/api/logs/**").hasRole("ADMIN")

                // ============================================
                // ADMIN ONLY: Quản lý sản phẩm / danh mục / KM / bàn
                // ============================================
                .requestMatchers("/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/promotions/**").hasRole("ADMIN")
                .requestMatchers("/api/tables/**").hasRole("ADMIN")

                // ============================================
                // USERS:
                // - GET → ADMIN + EMPLOYEE (Staff cần chọn nhân viên khi tạo đơn)
                // - POST/PUT/DELETE → chỉ ADMIN
                // ============================================
                .requestMatchers(HttpMethod.GET, "/api/users/**")
                    .hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // ============================================
                // CÒN LẠI: phải đăng nhập
                // ============================================
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:3003",
            "http://localhost:3000"
        ));

        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));

        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}