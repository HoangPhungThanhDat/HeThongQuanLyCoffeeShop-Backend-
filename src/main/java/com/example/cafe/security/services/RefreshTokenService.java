package com.example.cafe.security.services;

import com.example.cafe.entity.RefreshToken;
import com.example.cafe.entity.User;
import com.example.cafe.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * ✅ Tạo refresh token mới (dùng cho REFRESH — không xóa token cũ)
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString() + "." + UUID.randomUUID())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * ✅ Tạo refresh token cho LOGIN — xóa hết token cũ trước
     */
    @Transactional
    public RefreshToken createRefreshTokenForLogin(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString() + "." + UUID.randomUUID())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * ✅ FIX RACE CONDITION: Dùng deleteByTokenString (native query)
     * - Trả về số dòng bị xóa
     * - KHÔNG throw nếu token đã bị xóa bởi request khác
     */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        System.out.println("🔍 [VERIFY] Looking for: " + token.substring(0, 20) + "...");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    System.out.println("❌ [VERIFY] Token NOT FOUND in DB");
                    return new RuntimeException("Refresh token không tồn tại");
                });

        System.out.println("🔍 [VERIFY] Found token #" + refreshToken.getId()
                + ", revoked=" + refreshToken.isRevoked()
                + ", expiry=" + refreshToken.getExpiryDate());

        if (refreshToken.isRevoked()) {
            System.out.println("❌ [VERIFY] Token REVOKED");
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            System.out.println("❌ [VERIFY] Token EXPIRED");
            refreshTokenRepository.deleteByTokenString(token);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại");
        }

        // ✅ FIX: Native delete — không throw khi token đã bị xóa bởi request khác
        int deleted = refreshTokenRepository.deleteByTokenString(token);
        System.out.println("✅ [VERIFY] Deleted rows: " + deleted);

        if (deleted == 0) {
            System.out.println("⚠️ [VERIFY] Token already deleted by another request");
            throw new RuntimeException("Refresh token đã được sử dụng. Vui lòng thử lại");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }
}