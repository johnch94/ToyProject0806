package com.example.demo.user;

import com.example.demo.user.dto.UserJoinRequest;
import com.example.demo.user.dto.UserLoginRequest;
import com.example.demo.user.dto.UserLoginResponse;
import com.example.demo.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public UserResponse joinUser(UserJoinRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명 입니다 :" + request.getUsername());
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .role(UserRole.USER)
                .build();

        UserEntity savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * 로그인
     */
    public UserLoginResponse loginUser (UserLoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        if(!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 다릅니다.");
        }

        return UserLoginResponse.success(user);
    }

    /**
     * 사용자 상세 조회
     */
    public UserResponse getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        return UserResponse.fromEntity(user);
    }

    /**
     * 사용자명으로 조회
     */
    public UserResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username));

        return UserResponse.fromEntity(user);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        userRepository.delete(user);
        log.info("사용자 삭제 완료 - ID: {}, 사용자명: {}", userId, user.getUsername());
    }

    public UserEntity getAuthorByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다: "+ username ));
    }
}
