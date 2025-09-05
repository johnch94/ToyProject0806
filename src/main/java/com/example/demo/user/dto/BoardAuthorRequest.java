package com.example.demo.user.dto;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardAuthorRequest {

    private Long userId;
    private String username;
    private String email;
    private UserRole role = UserRole.USER;

    public static BoardAuthorRequest fromEntity(UserEntity entity) {
        return BoardAuthorRequest.builder()
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}
