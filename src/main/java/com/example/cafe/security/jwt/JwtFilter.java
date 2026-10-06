// package com.example.cafe.security.jwt;

// import com.example.cafe.repository.UserRepository;
// import com.example.cafe.security.services.JwtService;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.util.List;

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

//         String path = request.getRequestURI();

//         // ✅ Bỏ qua payment API — không check JWT
//         if (path.startsWith("/api/payment/") || path.startsWith("/api/momo/")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         final String authHeader = request.getHeader("Authorization");

//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         try {
//             String jwt = authHeader.substring(7);
//             String username = jwtService.extractUsername(jwt);

//             if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                 var userOpt = userRepository.findByUsername(username);

//                 // ✅ Dùng isTokenValid(String, String) — đã thêm vào JwtService
//                 if (userOpt.isPresent() && jwtService.isTokenValid(jwt, username)) {
//                     var user = userOpt.get();

//                     // ✅ Cấp quyền cho user (ROLE_xxx)
//                     List<SimpleGrantedAuthority> authorities =
//                             List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

//                     UsernamePasswordAuthenticationToken authToken =
//                             new UsernamePasswordAuthenticationToken(user, null, authorities);
//                     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                     SecurityContextHolder.getContext().setAuthentication(authToken);

//                     System.out.println("✅ JWT validated for user: " + username);
//                 } else {
//                     System.out.println("❌ Invalid JWT for user: " + username);
//                 }
//             }
//         } catch (Exception e) {
//             System.out.println("❌ JWT error: " + e.getMessage());
//         }

//         filterChain.doFilter(request, response);
//     }
// }












package com.example.cafe.security.jwt;

import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        String path = request.getRequestURI();

        // ✅ Bỏ qua payment API — không check JWT
        if (path.startsWith("/api/payment/") || path.startsWith("/api/momo/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userOpt = userRepository.findByUsername(username);

                if (userOpt.isPresent() && jwtService.isTokenValid(jwt, username)) {
                    var user = userOpt.get();

                    // ✅ Cấp quyền cho user (ROLE_xxx)
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    // ✅ FIX: Dùng username String làm principal (chuẩn Spring Security)
                    // → auth.getName() sẽ trả về đúng username
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,           // 👈 principal = String username
                                    null,
                                    authorities
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // ✅ Lưu User entity vào request attribute (nếu cần dùng ở chỗ khác)
                    request.setAttribute("currentUser", user);

                    System.out.println("✅ JWT validated for user: " + username
                            + " | roles: " + authorities);
                } else {
                    System.out.println("❌ Invalid JWT for user: " + username);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ JWT error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}