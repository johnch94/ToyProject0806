package com.example.demo.configuration;

import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security + JWT 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf(csrf -> csrf.disable())
            
            // 세션 사용 안 함 (JWT는 Stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인증/인가 규칙 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/auth/**",           // 회원가입, 로그인
                    "/api/health",            // 헬스체크
                    "/h2-console/**",         // H2 콘솔
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**"         // OpenAPI 문서
                ).permitAll()
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // H2 콘솔용 설정
            .headers(headers -> headers.frameOptions().disable())
            
            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 앞에 배치)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
    
    /**
     * PasswordEncoder Bean 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
