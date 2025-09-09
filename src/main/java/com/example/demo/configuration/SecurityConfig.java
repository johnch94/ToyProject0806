package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Spring Security 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 비활성화 (REST API)
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 경로
                .requestMatchers("/api/auth/**").permitAll()           // 인증 관련 API
                .requestMatchers("/api/boards/**").permitAll()        // 게시글 조회 (임시)
                .requestMatchers("/api/riot/**").permitAll()          // Riot API (외부 API 연동)
                .requestMatchers("/api/users/check/**").permitAll()   // 중복 확인
                .requestMatchers("/h2-console/**").permitAll()        // H2 콘솔 (개발용)
                .requestMatchers("/api/health/**").permitAll()        // 헬스체크 (새로운 경로)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger
                // 관리자만 접근 가능
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                // 나머지 요청은 인증 필요
                .anyRequest().authenticated()
            )
            // H2 콘솔을 위한 헤더 설정
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );
        
        return http.build();
    }
}
