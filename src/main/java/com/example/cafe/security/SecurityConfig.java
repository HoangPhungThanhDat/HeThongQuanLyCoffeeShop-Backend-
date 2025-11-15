// package com.example.cafe.security;

// import com.example.cafe.security.jwt.JwtFilter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.Arrays;
// import java.util.List;

// @Configuration
// public class SecurityConfig {

//         private final JwtFilter jwtFilter;

//         public SecurityConfig(JwtFilter jwtFilter) {
//                 this.jwtFilter = jwtFilter;
//         }

//         @Bean
//         public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//                 http

//                                 .csrf(csrf -> csrf.disable())

//                                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))

//                                 // Cấu hình quyền truy cập
//                                 .authorizeHttpRequests(auth -> auth
//                                                 // Cho phép không cần token
//                                                 .requestMatchers(
//                                                                 "/api/auth/**", // Đăng nhập / đăng ký
//                                                                 "/api/products/image/**",
//                                                                 "/api/users/image/**"

//                                                 ).permitAll()
//                                                 .requestMatchers(HttpMethod.GET,
//                                                                 "/api/products/**",
//                                                                 "/api/products/category/**",
//                                                                 "/api/tables/**",
//                                                                 "/api/categories/**"

//                                                 ).permitAll()
//                                                 .requestMatchers(HttpMethod.POST,
//                                                                 "/api/orders/**",
//                                                                 "/api/bills/**",
//                                                                 "/api/order-items/**")
//                                                 .permitAll()
//                                                 .requestMatchers(HttpMethod.PUT,
//                                                                 "/api/products/**",
//                                                                 "/api/tables/**")
//                                                 .permitAll()
//                                                 .anyRequest().authenticated())
//                                 .sessionManagement(session -> session
//                                                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//                 // 🔄 Thêm JWT Filter trước UsernamePasswordAuthenticationFilter
//                 http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

//                 return http.build();
//         }

//         @Bean
//         public CorsConfigurationSource corsConfigurationSource() {
//                 CorsConfiguration configuration = new CorsConfiguration();

//                 // 🌍 Cho phép các frontend (React)
//                 configuration.setAllowedOrigins(Arrays.asList(
//                                 "http://localhost:5173",
//                                 "http://localhost:5174",
//                                 "http://localhost:3003",
//                                 "http://localhost:3000"));

//                 // Cho phép các phương thức HTTP
//                 configuration.setAllowedMethods(Arrays.asList(
//                                 "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

//                 // Cho phép các header
//                 configuration.setAllowedHeaders(Arrays.asList(
//                                 "Authorization", "Content-Type", "Accept", "X-Requested-With"));
//                 // Cho phép gửi credentials (JWT header)
//                 configuration.setAllowCredentials(true);

//                 // Header trả về cho client
//                 configuration.setExposedHeaders(List.of("Authorization"));

//                 // Cache preflight request
//                 configuration.setMaxAge(3600L);

//                 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//                 source.registerCorsConfiguration("/api/**", configuration);

//                 return source;
//         }

//         @Bean
//         public PasswordEncoder passwordEncoder() {
//                 return new BCryptPasswordEncoder();
//         }

//         @Bean
//         public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//                 return config.getAuthenticationManager();
//         }
// }























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
            
            // Cấu hình quyền truy cập
            .authorizeHttpRequests(auth -> auth
                // ✅ CHO PHÉP PAYMENT API (KHÔNG CẦN TOKEN)
                .requestMatchers("/api/payment/**").permitAll()
                .requestMatchers("/api/momo/**").permitAll() 
                // Cho phép không cần token
                .requestMatchers(
                    "/api/auth/**",
                    "/api/products/image/**",
                    "/api/users/image/**"
                ).permitAll()
                
                .requestMatchers(HttpMethod.GET,
                    "/api/products/**",
                    "/api/products/category/**",
                    "/api/tables/**",
                    "/api/categories/**",
                    "/api/orders/**"
                ).permitAll()
                
                .requestMatchers(HttpMethod.POST,
                    "/api/orders/**",
                    "/api/bills/**",
                    "/api/order-items/**"
                ).permitAll()
                
                .requestMatchers(HttpMethod.PUT,
                    "/api/products/**",
                    "/api/tables/**"
                ).permitAll()
                
                .anyRequest().authenticated()
            )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // 🔄 Thêm JWT Filter trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌍 Cho phép các frontend (React)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:3003",
            "http://localhost:3000",
            // Production - Socket.IO
            "https://hethongquanlycoffeeshop-socketio-production.up.railway.app",
            "https://he-thong-quan-ly-coffee-shop-fronte.vercel.app"
        ));

        // Cho phép các phương thức HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Cho phép các header
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));
        
        // Cho phép gửi credentials (JWT header)
        configuration.setAllowCredentials(true);

        // Header trả về cho client
        configuration.setExposedHeaders(List.of("Authorization"));

        // Cache preflight request
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