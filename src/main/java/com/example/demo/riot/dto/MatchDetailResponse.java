package com.example.demo.riot.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 🎮 경기별 상세 전적 DTO
 * 
 * 기존 문제: queueId, mapId 같은 의미없는 데이터만 저장
 * 개선: 실제 전적 분석에 필요한 데이터 위주로 구성
 * 
 * 이 데이터로 가능한 분석:
 * - 승률 계산 (victory)
 * - KDA 분석 (kills, deaths, assists)  
 * - 성장률 분석 (cs, goldEarned)
 * - 주력 챔피언 분석 (championName)
 * - 게임 스타일 분석 (totalDamage, gameLength)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailResponse {
    // 기본 정보
    private String matchId;
    private LocalDateTime gameDate;
    private long gameLength;        // 게임 시간 (초)
    private String queueType;       // "솔로랭크", "자유랭크" 등
    
    // 🔥 핵심: 플레이어 성과 데이터 (전적 분석의 핵심!)
    private String championName;    // 플레이한 챔피언
    private boolean victory;        // 승리 여부
    private int kills;             // 킬 수
    private int deaths;            // 데스 수  
    private int assists;           // 어시스트 수
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
}
