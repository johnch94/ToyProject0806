package com.example.demo.user.dto;

import com.example.demo.user.UserRole;
import lombok.*;

/**
 * 로그인 응답 DTO (토이프로젝트 버전)
 * 
 * MVP 수준: 기본 사용자 정보 + JWT 토큰만 포함
 * 나중에 추가 가능: 토큰 만료시간, 리프레시 토큰
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private String token;    // JWT 토큰
    private String message;
}
