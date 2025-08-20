package com.example.demo.riot.dto;

import lombok.*;

/**
 * 플레이어 기본 정보 DTO
 * 토이프로젝트용 간단한 응답 모델
 * 
 * 실제 서비스에서는:
 * - 캐싱 가능한 데이터로 설계
 * - 버전 관리를 위한 @JsonIgnoreProperties 추가
 * - 유효성 검증을 위한 @Valid 어노테이션 추가
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiorResponse {
    private String gameName;
    private String tagLine;
    private String puuid;
    private String summonerName;
    private int summonerLevel;
    private String tier;
    private String rank;
    private int leaguePoints;
}
