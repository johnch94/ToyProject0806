package com.example.demo.user.dto;

import com.example.demo.user.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdDate;

    public static UserResponse fromEntity(UserEntity entity) {
        return UserResponse.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole().name())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
