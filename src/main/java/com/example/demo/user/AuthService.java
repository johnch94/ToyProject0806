package com.example.demo.user;

import com.example.demo.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signup(UserCreateRequest request) {
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("이미 존재하는 사용자명입니다: " + request.getUsername());
        }
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        
        UserEntity savedUser = userRepository.save(user);
        log.info("회원가입 완료 - 사용자명: {}, 이메일: {}", savedUser.getUsername(), savedUser.getEmail());
        
        return UserResponse.fromEntity(savedUser);
    }
    
    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다."));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
        
        log.info("로그인 성공 - 사용자명: {}", user.getUsername());
        
        // TODO: JWT 토큰 생성 (추후 구현)
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .token(null)  // JWT 토큰 추후 구현
                .message("로그인 성공")
                .build();
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