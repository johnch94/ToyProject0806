package com.example.demo.user;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러 (토이프로젝트 버전)
 * 
 * MVP 수준: 회원가입, 로그인, 중복확인만 구현
 * 기존 네이밍 규칙 유지: ~Controller, ~Request, ~Response
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // 프론트엔드 연동을 위한 CORS 설정
public class AuthController {
    
    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입 성공", user));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * 
     * JWT는 클라이언트에서 토큰 삭제로 처리 (서버는 상태 유지 안함)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    /**
     * 사용자명 중복 확인
     * GET /api/auth/check/username/{username}
     */
    @GetMapping("/check/username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean available = authService.isUsernameAvailable(username);
        String message = available ? "사용 가능한 사용자명" : "이미 사용 중인 사용자명";
        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 이메일 중복 확인
     * GET /api/auth/check/email/{email}
     */
    @GetMapping("/check/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@PathVariable String email) {
        boolean available = authService.isEmailAvailable(email);
        String message = available ? "사용 가능한 이메일" : "이미 사용 중인 이메일";
        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 헬스 체크
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Auth API 서버 정상 작동", "OK"));
    }
}
