package com.example.demo.riot.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 🎮 단일 경기 상세 조회 응답 DTO
 * 
 * 용도: 특정 플레이어의 특정 경기 상세 정보
 * URL: GET /api/riot/match/{matchId}?puuid={puuid}
 * 
 * 기존 MatchDetailResponse와 차이:
 * - 플레이어 정보 포함 (gameName, tagLine)
 * - 단독으로 사용 가능한 완전한 응답
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailWithPlayerResponse {
    
    // 플레이어 정보
    private String puuid;
    private String gameName;
    private String tagLine;
    
    // 경기 기본 정보
    private String matchId;
    private LocalDateTime gameDate;
    private long gameLength;        // 게임 시간 (초)
    private String queueType;       // "솔로랭크", "자유랭크" 등
    
    // 플레이어 성과 데이터
    private String championName;
    private boolean victory;
    private int kills;
    private int deaths;
    private int assists;
    private int cs;                // CS (미니언 + 정글 몬스터)
    private int totalDamage;       // 총 딜량
    private int goldEarned;        // 획득 골드
    
    /**
     * 🔧 편의 메서드: KDA 계산
     */
    public double getKDA() {
        return deaths > 0 ? (double) (kills + assists) / deaths : (double) (kills + assists);
    }
    
    /**
     * 🔧 편의 메서드: 분당 CS
     */
    public double getCSPerMinute() {
        return gameLength > 0 ? (double) cs / (gameLength / 60.0) : 0.0;
    }
    
    /**
     * 🔧 편의 메서드: 플레이어 표시명
     */
    public String getPlayerDisplayName() {
        return gameName + "#" + tagLine;
    }
    
    /**
     * 🔧 편의 메서드: 게임 시간 포맷 (30분 25초)
     */
    public String getFormattedGameLength() {
        long minutes = gameLength / 60;
        long seconds = gameLength % 60;
        return String.format("%d분 %d초", minutes, seconds);
    }
}
