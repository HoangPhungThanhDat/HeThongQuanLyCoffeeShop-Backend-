// package com.example.cafe.controllers;

// import com.example.cafe.dto.AuthResponse;
// import com.example.cafe.dto.LoginDto;
// import com.example.cafe.entity.RefreshToken;
// import com.example.cafe.entity.User;
// import com.example.cafe.entity.enums.Role;
// import com.example.cafe.repository.UserRepository;
// import com.example.cafe.security.services.JwtService;
// import com.example.cafe.security.services.RefreshTokenService;

// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.ResponseCookie;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;

// import java.time.Duration;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {

//     private final UserRepository userRepository;
//     private final JwtService jwtService;
//     private final PasswordEncoder passwordEncoder;
//     private final RefreshTokenService refreshTokenService;

//     @Value("${jwt.refresh-token-expiration}")
//     private long refreshTokenExpiration;

//     @Value("${jwt.access-token-expiration}")
//     private long accessTokenExpiration;

//     // ✅ 2 cookie name khác nhau cho 2 portal
//     private static final String COOKIE_ADMIN = "refreshTokenAdmin";
//     private static final String COOKIE_STAFF = "refreshTokenStaff";
//     private static final String COOKIE_DEFAULT = "refreshToken";

//     public AuthController(UserRepository userRepository,
//             JwtService jwtService,
//             PasswordEncoder passwordEncoder,
//             RefreshTokenService refreshTokenService) {
//         this.userRepository = userRepository;
//         this.jwtService = jwtService;
//         this.passwordEncoder = passwordEncoder;
//         this.refreshTokenService = refreshTokenService;
//     }

//     // ==================== REGISTER ====================
//     @PostMapping("/register")
//     public ResponseEntity<?> register(@RequestBody User request) {
//         if (userRepository.existsByUsername(request.getUsername())) {
//             return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
//         }
//         if (userRepository.existsByEmail(request.getEmail())) {
//             return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
//         }

//         User newUser = User.builder()
//                 .username(request.getUsername())
//                 .password(passwordEncoder.encode(request.getPassword()))
//                 .fullName(request.getFullName())
//                 .email(request.getEmail())
//                 .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
//                 .isActive(true)
//                 .build();

//         userRepository.save(newUser);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message", "Registered successfully");
//         return ResponseEntity.ok(response);
//     }

//     // ==================== LOGIN ====================
//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody LoginDto request) {
//         System.out.println("\n========================================================");
//         System.out.println("🔥 [LOGIN] Username: " + request.getUsername());
//         System.out.println("🔥 [LOGIN] Portal: " + request.getPortal());
//         System.out.println("========================================================");

//         try {
//             User user = userRepository.findByUsername(request.getUsername())
//                     .orElseThrow(() -> new RuntimeException("User not found"));

//             if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//                 System.out.println("❌ [LOGIN] Password mismatch");
//                 return ResponseEntity.status(401).body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
//             }

//             // ==================== KIỂM TRA ROLE THEO PORTAL ====================
//             String portal = request.getPortal();
//             Role userRole = user.getRole();

//             if ("admin".equalsIgnoreCase(portal)) {
//                 if (userRole != Role.ADMIN) {
//                     System.out.println("❌ [LOGIN] User " + user.getUsername()
//                             + " (role=" + userRole + ") cố đăng nhập vào ADMIN portal");
//                     return ResponseEntity.status(403).body(
//                             Map.of("error", "Chỉ ADMIN mới được phép đăng nhập vào trang quản trị"));
//                 }
//             } else if ("staff".equalsIgnoreCase(portal)) {
//                 if (userRole != Role.ADMIN && userRole != Role.EMPLOYEE) {
//                     System.out.println("❌ [LOGIN] User " + user.getUsername()
//                             + " (role=" + userRole + ") cố đăng nhập vào STAFF portal");
//                     return ResponseEntity.status(403).body(
//                             Map.of("error", "Chỉ ADMIN hoặc EMPLOYEE mới được phép đăng nhập"));
//                 }
//             } else {
//                 System.out.println("⚠️ [LOGIN] Không có portal — bỏ qua check role");
//             }

//             // ==================== SINH TOKEN ====================
//             String accessToken = jwtService.generateAccessToken(user);

//             // ✅ Dùng createRefreshTokenForLogin — tự động xóa RT cũ + tạo mới
//             RefreshToken refreshToken = refreshTokenService.createRefreshTokenForLogin(user);

//             String cookieName = getCookieName(portal);
//             String cookieValue = buildRefreshTokenCookie(cookieName, refreshToken.getToken());
//             System.out.println("🍪 [LOGIN] Set-Cookie (" + cookieName + "): " + cookieValue);

//             AuthResponse body = AuthResponse.builder()
//                     .id(user.getId())
//                     .username(user.getUsername())
//                     .email(user.getEmail())
//                     .roles(List.of(user.getRole().name()))
//                     .accessToken(accessToken)
//                     .tokenType("Bearer")
//                     .expiresIn(accessTokenExpiration / 1000)
//                     .build();

//             System.out.println("✅ [LOGIN] Success — role=" + userRole + ", cookie=" + cookieName);
//             System.out.println("========================================================\n");

//             return ResponseEntity.ok()
//                     .header(HttpHeaders.SET_COOKIE, cookieValue)
//                     .body(body);

//         } catch (Exception e) {
//             System.out.println("❌ [LOGIN] EXCEPTION: " + e.getMessage());
//             e.printStackTrace();
//             System.out.println("========================================================\n");
//             return ResponseEntity.status(401).body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
//         }
//     }

//     // ==================== REFRESH ====================
//     @PostMapping("/refresh")
//     public ResponseEntity<?> refresh(HttpServletRequest request) {
//         // ✅ Đọc portal từ header X-Portal
//         String portal = request.getHeader("X-Portal");
//         String cookieName = getCookieName(portal);

//         System.out.println("\n========================================================");
//         System.out.println("🔄 [REFRESH] Portal: " + portal);
//         System.out.println("🔄 [REFRESH] Cookie name: " + cookieName);
//         System.out.println("🔄 [REFRESH] Cookies count: "
//                 + (request.getCookies() != null ? request.getCookies().length : "null"));
//         System.out.println("========================================================");

//         try {
//             String refreshTokenStr = getCookieValue(request, cookieName);
//             if (refreshTokenStr == null) {
//                 System.out.println("❌ [REFRESH] No refresh token (looking for " + cookieName + ")");
//                 return ResponseEntity.status(401).body(Map.of("error", "Không có refresh token"));
//             }
//             System.out.println("🔄 [REFRESH] Token found: " + refreshTokenStr.substring(0, 20) + "...");

//             // ✅ verifyRefreshToken ĐÃ XÓA token cũ (rotation) — chỉ cần tạo mới
//             RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
//             User user = refreshToken.getUser();
//             System.out.println("🔄 [REFRESH] User: " + user.getUsername());

//             String newAccessToken = jwtService.generateAccessToken(user);

//             // ✅ createRefreshToken — KHÔNG xóa (vì verify đã xóa rồi)
//             RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
//             String cookieValue = buildRefreshTokenCookie(cookieName, newRefreshToken.getToken());

//             System.out.println("🍪 [REFRESH] New Set-Cookie (" + cookieName + "): " + cookieValue);

//             AuthResponse body = AuthResponse.builder()
//                     .id(user.getId())
//                     .username(user.getUsername())
//                     .email(user.getEmail())
//                     .roles(List.of(user.getRole().name()))
//                     .accessToken(newAccessToken)
//                     .tokenType("Bearer")
//                     .expiresIn(accessTokenExpiration / 1000)
//                     .build();

//             System.out.println("✅ [REFRESH] Success");
//             System.out.println("========================================================\n");

//             return ResponseEntity.ok()
//                     .header(HttpHeaders.SET_COOKIE, cookieValue)
//                     .body(body);

//         } catch (Exception e) {
//             System.out.println("❌ [REFRESH] EXCEPTION: " + e.getMessage());
//             System.out.println("========================================================\n");

//             return ResponseEntity.status(401)
//                     .header(HttpHeaders.SET_COOKIE, buildClearCookie(cookieName))
//                     .body(Map.of("error", e.getMessage()));
//         }
//     }

//     // ==================== LOGOUT ====================
//     @PostMapping("/logout")
//     public ResponseEntity<?> logout(HttpServletRequest request) {
//         String portal = request.getHeader("X-Portal");
//         String cookieName = getCookieName(portal);

//         System.out.println("🚪 [LOGOUT] Portal: " + portal + ", cookie: " + cookieName);

//         String refreshTokenStr = getCookieValue(request, cookieName);
//         if (refreshTokenStr != null) {
//             try {
//                 // ✅ verify đã xóa token rồi, chỉ cần lấy user để xóa sạch
//                 RefreshToken rt = refreshTokenService.verifyRefreshToken(refreshTokenStr);
//                 refreshTokenService.deleteByUser(rt.getUser());
//                 System.out.println("🚪 [LOGOUT] Deleted token from DB");
//             } catch (Exception ignored) {
//             }
//         }

//         return ResponseEntity.ok()
//                 .header(HttpHeaders.SET_COOKIE, buildClearCookie(cookieName))
//                 .body(Map.of("message", "Đăng xuất thành công"));
//     }

//     // ==================== HELPERS ====================

//     /**
//      * ✅ Xác định cookie name theo portal
//      */
//     private String getCookieName(String portal) {
//         if ("admin".equalsIgnoreCase(portal)) return COOKIE_ADMIN;
//         if ("staff".equalsIgnoreCase(portal)) return COOKIE_STAFF;
//         return COOKIE_DEFAULT;
//     }

//     /**
//      * ✅ Build Set-Cookie header để SET refresh token (HttpOnly)
//      */
//     private String buildRefreshTokenCookie(String cookieName, String token) {
//         return ResponseCookie.from(cookieName, token)
//                 .httpOnly(true)
//                 .secure(false)
//                 .sameSite("Lax")
//                 .path("/")
//                 .maxAge(Duration.ofMillis(refreshTokenExpiration))
//                 .build()
//                 .toString();
//     }

//     /**
//      * ✅ Build Set-Cookie header để XÓA refresh token
//      */
//     private String buildClearCookie(String cookieName) {
//         return ResponseCookie.from(cookieName, "")
//                 .httpOnly(true)
//                 .secure(false)
//                 .sameSite("Lax")
//                 .path("/")
//                 .maxAge(0)
//                 .build()
//                 .toString();
//     }

//     /**
//      * ✅ Đọc giá trị cookie theo name
//      */
//     private String getCookieValue(HttpServletRequest request, String cookieName) {
//         if (request.getCookies() == null)
//             return null;
//         for (Cookie c : request.getCookies()) {
//             if (cookieName.equals(c.getName()))
//                 return c.getValue();
//         }
//         return null;
//     }
// }
























package com.example.cafe.controllers;

import com.example.cafe.dto.AuthResponse;
import com.example.cafe.dto.LoginDto;
import com.example.cafe.entity.RefreshToken;
import com.example.cafe.entity.User;
import com.example.cafe.entity.enums.Role;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.JwtService;
import com.example.cafe.security.services.RefreshTokenService;
import com.example.cafe.services.ActivityLogService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private static final String COOKIE_ADMIN = "refreshTokenAdmin";
    private static final String COOKIE_STAFF = "refreshTokenStaff";
    private static final String COOKIE_DEFAULT = "refreshToken";

    public AuthController(UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            ActivityLogService activityLogService) {          // 👈 MỚI
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.activityLogService = activityLogService;       // 👈 MỚI
    }

    // ==================== REGISTER ====================
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
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .isActive(true)
                .build();

        userRepository.save(newUser);

        // ✅ GHI LOG REGISTER
        activityLogService.saveLog(
                com.example.cafe.entity.enums.LogAction.CREATE,
                com.example.cafe.entity.enums.LogLevel.SUCCESS,
                "Người dùng",
                newUser.getUsername(),
                "Đăng ký tài khoản mới",
                "Tạo tài khoản '" + newUser.getUsername()
                        + "' với vai trò " + newUser.getRole()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registered successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== LOGIN ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto request) {
        System.out.println("\n========================================================");
        System.out.println("🔥 [LOGIN] Username: " + request.getUsername());
        System.out.println("🔥 [LOGIN] Portal: " + request.getPortal());
        System.out.println("========================================================");

        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("❌ [LOGIN] Password mismatch");
                // ✅ GHI LOG LOGIN FAIL (sai pass)
                activityLogService.logLoginByUsername(request.getUsername(), false);
                return ResponseEntity.status(401).body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
            }

            // ==================== KIỂM TRA ROLE THEO PORTAL ====================
            String portal = request.getPortal();
            Role userRole = user.getRole();

            if ("admin".equalsIgnoreCase(portal)) {
                if (userRole != Role.ADMIN) {
                    System.out.println("❌ [LOGIN] User " + user.getUsername()
                            + " (role=" + userRole + ") cố đăng nhập vào ADMIN portal");
                    // ✅ GHI LOG LOGIN FAIL (sai role)
                    activityLogService.logLoginByUsername(request.getUsername(), false);
                    return ResponseEntity.status(403).body(
                            Map.of("error", "Chỉ ADMIN mới được phép đăng nhập vào trang quản trị"));
                }
            } else if ("staff".equalsIgnoreCase(portal)) {
                if (userRole != Role.ADMIN && userRole != Role.EMPLOYEE) {
                    System.out.println("❌ [LOGIN] User " + user.getUsername()
                            + " (role=" + userRole + ") cố đăng nhập vào STAFF portal");
                    activityLogService.logLoginByUsername(request.getUsername(), false);
                    return ResponseEntity.status(403).body(
                            Map.of("error", "Chỉ ADMIN hoặc EMPLOYEE mới được phép đăng nhập"));
                }
            } else {
                System.out.println("⚠️ [LOGIN] Không có portal — bỏ qua check role");
            }

            // ==================== SINH TOKEN ====================
            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshTokenForLogin(user);

            String cookieName = getCookieName(portal);
            String cookieValue = buildRefreshTokenCookie(cookieName, refreshToken.getToken());
            System.out.println("🍪 [LOGIN] Set-Cookie (" + cookieName + "): " + cookieValue);

            AuthResponse body = AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(List.of(user.getRole().name()))
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000)
                    .build();

            System.out.println("✅ [LOGIN] Success — role=" + userRole + ", cookie=" + cookieName);
            System.out.println("========================================================\n");

            // ✅ GHI LOG LOGIN SUCCESS
            activityLogService.logLoginByUsername(request.getUsername(), true);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookieValue)
                    .body(body);

        } catch (Exception e) {
            System.out.println("❌ [LOGIN] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================================\n");
            // ✅ GHI LOG LOGIN FAIL (exception)
            activityLogService.logLoginByUsername(request.getUsername(), false);
            return ResponseEntity.status(401).body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
        }
    }

    // ==================== REFRESH ====================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String portal = request.getHeader("X-Portal");
        String cookieName = getCookieName(portal);

        System.out.println("\n========================================================");
        System.out.println("🔄 [REFRESH] Portal: " + portal);
        System.out.println("🔄 [REFRESH] Cookie name: " + cookieName);
        System.out.println("🔄 [REFRESH] Cookies count: "
                + (request.getCookies() != null ? request.getCookies().length : "null"));
        System.out.println("========================================================");

        try {
            String refreshTokenStr = getCookieValue(request, cookieName);
            if (refreshTokenStr == null) {
                System.out.println("❌ [REFRESH] No refresh token (looking for " + cookieName + ")");
                return ResponseEntity.status(401).body(Map.of("error", "Không có refresh token"));
            }
            System.out.println("🔄 [REFRESH] Token found: " + refreshTokenStr.substring(0, 20) + "...");

            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
            User user = refreshToken.getUser();
            System.out.println("🔄 [REFRESH] User: " + user.getUsername());

            String newAccessToken = jwtService.generateAccessToken(user);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            String cookieValue = buildRefreshTokenCookie(cookieName, newRefreshToken.getToken());

            System.out.println("🍪 [REFRESH] New Set-Cookie (" + cookieName + "): " + cookieValue);

            AuthResponse body = AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(List.of(user.getRole().name()))
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000)
                    .build();

            System.out.println("✅ [REFRESH] Success");
            System.out.println("========================================================\n");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookieValue)
                    .body(body);

        } catch (Exception e) {
            System.out.println("❌ [REFRESH] EXCEPTION: " + e.getMessage());
            System.out.println("========================================================\n");

            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, buildClearCookie(cookieName))
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== LOGOUT ====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String portal = request.getHeader("X-Portal");
        String cookieName = getCookieName(portal);

        System.out.println("🚪 [LOGOUT] Portal: " + portal + ", cookie: " + cookieName);

        String refreshTokenStr = getCookieValue(request, cookieName);
        String usernameForLog = null;

        if (refreshTokenStr != null) {
            try {
                RefreshToken rt = refreshTokenService.verifyRefreshToken(refreshTokenStr);
                usernameForLog = rt.getUser().getUsername();
                refreshTokenService.deleteByUser(rt.getUser());
                System.out.println("🚪 [LOGOUT] Deleted token from DB");
            } catch (Exception ignored) {
            }
        }

        // ✅ GHI LOG LOGOUT (chỉ ghi nếu biết username)
        if (usernameForLog != null) {
            activityLogService.logLogoutByUsername(usernameForLog);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildClearCookie(cookieName))
                .body(Map.of("message", "Đăng xuất thành công"));
    }

    // ==================== HELPERS ====================

    private String getCookieName(String portal) {
        if ("admin".equalsIgnoreCase(portal)) return COOKIE_ADMIN;
        if ("staff".equalsIgnoreCase(portal)) return COOKIE_STAFF;
        return COOKIE_DEFAULT;
    }

    private String buildRefreshTokenCookie(String cookieName, String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build()
                .toString();
    }

    private String buildClearCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString();
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null)
            return null;
        for (Cookie c : request.getCookies()) {
            if (cookieName.equals(c.getName()))
                return c.getValue();
        }
        return null;
    }
}