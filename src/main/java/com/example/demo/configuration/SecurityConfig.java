package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // GET만 쓰면 크게 상관없지만, POST 테스트 시 편의상 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/riot/**", "/error").permitAll() // 전적 API는 누구나 접근
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())   // 필요하면 기본 인증 유지
                .formLogin(form -> form.disable());    // 기본 로그인 폼 비활성
        return http.build();
    }
}
