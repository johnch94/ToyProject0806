package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증
 * 
 * 토이프로젝트 수준: 기본적인 토큰 생성/검증만 구현
 * 실무에서는 Refresh Token, 토큰 블랙리스트 등 추가 기능 필요
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    private final Key key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds:86400}") long tokenValidityInSeconds) {
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    /**
     * JWT 토큰 생성 (사용자명만 포함하는 심플한 버전)
     */
    public String createToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰 유효성 검증 (간단 버전)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            return false;
        }
    }
}
