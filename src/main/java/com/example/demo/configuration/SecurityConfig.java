package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 🔓 Spring Security 설정 - 테스트용 완전 개방
 * 
 * 모든 엔드포인트에 인증 없이 접근 가능하도록 설정
 * 실제 운영에서는 보안 설정 필요!
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // 모든 요청 허용
            )
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화
            .headers(headers -> headers.frameOptions().disable());  // H2 콘솔용
            
        return http.build();
    }
    
    /**
     * 🔐 PasswordEncoder Bean 등록
     * AuthService에서 필요로 하는 의존성
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
