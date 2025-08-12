package com.example.demo.board.dto;

import lombok.*;

// 작성자 통계 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorStatsResponse {
    private String author;
    private long totalPosts;
    private long recentPostCount;
}