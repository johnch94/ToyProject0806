package com.example.demo.user;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * 사용자 전체 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy) {
        
        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", users));
    }
    
    /**
     * 사용자 상세 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", user));
    }
    
    /**
     * 사용자명으로 조회
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("사용자 조회 성공", user));
    }
    
    /**
     * 사용자 생성 (회원가입)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 생성 성공", user));
    }
    
    /**
     * 사용자 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        
        UserResponse user = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("사용자 수정 성공", user));
    }
    
    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자 삭제 성공", null));
    }
    
    /**
     * 사용자 검색 (페이징)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<UserResponse> users = userService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("사용자 검색 성공", users));
    }
    
    /**
     * 역할별 사용자 조회
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<UserResponse> users = userService.getUsersByRole(role, page, size);
        return ResponseEntity.ok(ApiResponse.success("역할별 사용자 조회 성공", users));
    }
    
    /**
     * 최신 가입 사용자 10명 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getRecentUsers() {
        List<UserResponse> users = userService.getRecentUsers();
        return ResponseEntity.ok(ApiResponse.success("최신 사용자 목록 조회 성공", users));
    }
    
    /**
     * 사용자 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        UserStatsResponse stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success("사용자 통계 조회 성공", stats));
    }
    
    /**
     * 사용자명 중복 확인
     */
    @GetMapping("/check/username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        String message = exists ? "사용 불가능한 사용자명" : "사용 가능한 사용자명";
        return ResponseEntity.ok(ApiResponse.success(message, !exists));
    }
    
    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        String message = exists ? "사용 불가능한 이메일" : "사용 가능한 이메일";
        return ResponseEntity.ok(ApiResponse.success(message, !exists));
    }
}