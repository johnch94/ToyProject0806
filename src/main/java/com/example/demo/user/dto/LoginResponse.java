package com.example.demo.user.dto;

import com.example.demo.user.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private String token;  // JWT 토큰 (추후 구현)
    private String message;
}