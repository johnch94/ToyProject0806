package com.example.demo.user;

import com.example.demo.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // 추후 Security 설정에서 Bean 등록
    
    /**
     * 사용자 전체 조회 (페이징)
     */
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }
    
    /**
     * 사용자 상세 조회
     */
    public UserResponse getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        log.info("사용자 조회 - ID: {}, 사용자명: {}", userId, user.getUsername());
        return UserResponse.fromEntity(user);
    }
    
    /**
     * 사용자명으로 조회
     */
    public UserResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username));
        
        return UserResponse.fromEntity(user);
    }
    
    /**
     * 사용자 생성 (회원가입)
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
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
        log.info("사용자 생성 완료 - ID: {}, 사용자명: {}", savedUser.getUserId(), savedUser.getUsername());
        
        return UserResponse.fromEntity(savedUser);
    }
    
    /**
     * 사용자 수정
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        // 이메일 중복 체크 (다른 사용자가 사용 중인지)
        if (userRepository.existsByEmail(request.getEmail()) && 
            !user.getEmail().equals(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 이메일 수정
        user.updateEmail(request.getEmail());
        
        // 비밀번호 수정 (요청에 포함된 경우만)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.updatePassword(encodedPassword);
        }
        
        log.info("사용자 수정 완료 - ID: {}", userId);
        return UserResponse.fromEntity(user);
    }
    
    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        userRepository.delete(user);
        log.info("사용자 삭제 완료 - ID: {}", userId);
    }
    
    /**
     * 사용자명으로 검색 (페이징)
     */
    public Page<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByUsernameContaining(keyword, pageable)
                .map(UserResponse::fromEntity);
    }
    
    /**
     * 역할별 사용자 조회
     */
    public Page<UserResponse> getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByRole(role, pageable)
                .map(UserResponse::fromEntity);
    }
    
    /**
     * 최신 가입 사용자 10명 조회
     */
    public List<UserResponse> getRecentUsers() {
        return userRepository.findTop10ByOrderByCreatedDateDesc()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자 통계 조회
     */
    public UserStatsResponse getUserStats() {
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRole(UserRole.ADMIN);
        long regularUsers = userRepository.countByRole(UserRole.USER);
        
        // 최근 7일 가입자
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<UserEntity> recentSignups = userRepository.findByCreatedDateBetween(weekAgo, LocalDateTime.now());
        
        LocalDateTime lastSignupDate = userRepository.findTop10ByOrderByCreatedDateDesc()
                .stream()
                .findFirst()
                .map(UserEntity::getCreatedDate)
                .orElse(null);
        
        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .adminUsers(adminUsers)
                .regularUsers(regularUsers)
                .recentSignups(recentSignups.size())
                .lastSignupDate(lastSignupDate)
                .build();
    }
    
    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
    
    /**
     * 사용자명 중복 확인
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // 커스텀 예외 클래스들
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
    
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