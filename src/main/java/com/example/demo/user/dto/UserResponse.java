package com.example.demo.user.dto;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRole;
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
    private UserRole role;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Entity -> Response 변환
    public static UserResponse fromEntity(UserEntity entity) {
        return UserResponse.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }
}