package com.example.demo.user.dto;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponse {
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private String accessToken;
    private String message;

    public static UserLoginResponse success(UserEntity entity) {
        return UserLoginResponse.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .message("로그인 성공")
                .build();
    }

    public static UserLoginResponse fromEntity(UserEntity entity) {
        return UserLoginResponse.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}
