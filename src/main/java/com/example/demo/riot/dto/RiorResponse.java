package com.example.demo.riot.dto;

import lombok.*;

// 작성자 통계 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiorResponse {
    private String id;
    private String accountId;
    private String puuid;
    private String name;
}
