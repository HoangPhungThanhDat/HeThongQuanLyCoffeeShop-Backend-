// package com.example.cafe.security.jwt;

// import com.example.cafe.entity.User;
// import com.example.cafe.repository.UserRepository;
// import com.example.cafe.security.services.JwtService;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// public class JwtFilter extends OncePerRequestFilter {

//     private final JwtService jwtService;
//     private final UserRepository userRepository;

//     public JwtFilter(JwtService jwtService, UserRepository userRepository) {
//         this.jwtService = jwtService;
//         this.userRepository = userRepository;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain)
//             throws ServletException, IOException {

//         final String authHeader = request.getHeader("Authorization");
//         final String jwt;
//         final String username;

//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         jwt = authHeader.substring(7);
//         username = jwtService.extractUsername(jwt);

//         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//             var user = userRepository.findByUsername(username);
//             if (user.isPresent() && jwtService.isTokenValid(jwt, username)) {
//                 UsernamePasswordAuthenticationToken authToken =
//                         new UsernamePasswordAuthenticationToken(user.get(), null, null);
//                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                 SecurityContextHolder.getContext().setAuthentication(authToken);
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
// }




























package com.example.cafe.security.jwt;

import com.example.cafe.entity.User;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ BỎ QUA PAYMENT API - KHÔNG CHECK JWT
        String path = request.getRequestURI();
        
        if (path.startsWith("/api/payment/") || 
            path.startsWith("/api/momo/")
           ) {
            System.out.println("🔓 Bypassing JWT check for payment API: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Nếu không có Authorization header hoặc không bắt đầu bằng "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy JWT token (bỏ "Bearer " prefix)
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        // Nếu có username và chưa authenticate
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var user = userRepository.findByUsername(username);
            
            // Validate token
            if (user.isPresent() && jwtService.isTokenValid(jwt, username)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user.get(), null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                System.out.println("✅ JWT validated for user: " + username);
            } else {
                System.out.println("❌ Invalid JWT for user: " + username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
