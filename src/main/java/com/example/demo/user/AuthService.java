package com.example.demo.user;

import com.example.demo.security.JwtTokenProvider;
import com.example.demo.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 서비스 (토이프로젝트 버전)
 * 
 * MVP 수준: 회원가입, 로그인, 중복확인만 구현
 * 나중에 추가 가능: 비밀번호 찾기, 이메일 인증, 소셜 로그인
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signup(UserCreateRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("이미 존재하는 사용자명입니다: " + request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 사용자 생성
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        
        UserEntity savedUser = userRepository.save(user);
        log.info("회원가입 완료 - 사용자명: {}", savedUser.getUsername());
        
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * 로그인 (JWT 토큰 발급)
     */
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            // 2. JWT 토큰 생성
            String token = jwtTokenProvider.createToken(authentication);
            
            // 3. 사용자 정보 조회
            UserEntity user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));
            
            log.info("로그인 성공 - 사용자명: {}", user.getUsername());
            
            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .token(token)
                    .message("로그인 성공")
                    .build();
                    
        } catch (Exception e) {
            log.warn("로그인 실패 - 사용자명: {}", request.getUsername());
            throw new BadCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * 사용자명 중복 확인
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // 커스텀 예외 클래스들
    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String message) {
            super(message);
        }
    }
    
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }
}
