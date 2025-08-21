package com.example.demo.user.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {
    
    private long totalUsers;
    private long adminUsers;
    private long regularUsers;
    private long recentSignups;  // 최근 7일
    private LocalDateTime lastSignupDate;
}