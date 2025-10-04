package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private long tokenValidityInMilliseconds;

    private SecretKey secretKey;

    /**
     * 빈 생성 후 SecretKey 초기화
     */
    @PostConstruct
    protected void init() {
        // HMAC-SHA 알고리즘을 위한 SecretKey 생성
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 생성
     * 
     * @param username 사용자명
     * @param role 사용자 권한
     * @return JWT 토큰 문자열
     */
    public String createToken(String username, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(username)                      // 토큰 제목 (사용자명)
                .claim("role", role)                    // 커스텀 클레임 (권한)
                .issuedAt(now)                          // 발행 시간
                .expiration(validity)                   // 만료 시간
                .signWith(secretKey, Jwts.SIG.HS256)   // 서명 알고리즘
                .compact();
    }

    /**
     * JWT 토큰에서 사용자명 추출
     * 
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 권한 추출
     * 
     * @param token JWT 토큰
     * @return 권한
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰에서 Claims 추출 (private 메서드)
     * 
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
