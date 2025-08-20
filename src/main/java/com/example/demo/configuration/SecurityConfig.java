package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 
 * 토이프로젝트 보안 정책:
 * - Riot API는 공개 접근 허용 (게임 데이터 조회)
 * - Board API는 인증 필요 (게시글 작성/수정/삭제)
 * - User API는 인증 필요 (회원 정보 관리)
 * 
 * 실제 서비스에서는:
 * - API별 세분화된 권한 설정 (ROLE_USER, ROLE_ADMIN)
 * - JWT 토큰 기반 인증
 * - CORS 설정으로 프론트엔드 도메인 허용
 * - Rate Limiting으로 API 남용 방지
 */
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 개발 편의상 비활성화 (운영에서는 활성화 권장)
                .authorizeHttpRequests(auth -> auth
                        // Riot API - 인증 없이 접근 가능 (공개 게임 데이터)
                        .requestMatchers("/riot/**", "/api/riot/**").permitAll()
                        
                        // 개발/테스트용 엔드포인트
                        .requestMatchers("/error", "/h2-console/**").permitAll()
                        
                        // Swagger/OpenAPI 문서 (개발용)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // Board API - 조회는 허용, 수정은 인증 필요 (추후 구현)
                        .requestMatchers("/api/board/**").permitAll() // 현재는 모두 허용
                        
                        // User API - 인증 필요 (추후 구현)
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        .requestMatchers("/api/user/**").authenticated()
                        
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())   // 기본 HTTP 인증 유지
                .formLogin(form -> form.disable());    // 폼 로그인 비활성화 (API 서버)
                
        // H2 콘솔 사용을 위한 추가 설정 (개발용)
        http.headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }
}
