package com.example.demo.riot.dto;

import lombok.*;

/**
 * 🎮 전적 통계 요약 DTO
 * 
 * 여러 경기 데이터를 분석해서 의미있는 통계 제공
 * 
 * 스토리 생성에 활용 가능한 데이터:
 * - "최근 5경기 80% 승률로 승승장구!"
 * - "아지르 장인! 최근 3경기 연속 플레이"  
 * - "평균 KDA 2.5로 안정적인 실력"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStatsResponse {
    // 기본 통계
    private int totalGames;         // 총 경기 수
    private int wins;               // 승리 수
    private int losses;             // 패배 수
    private double winRate;         // 승률 (%)
    
    // KDA 통계  
    private double averageKDA;      // 평균 KDA
    private int totalKills;         // 총 킬 수
    private int totalDeaths;        // 총 데스 수
    private int totalAssists;       // 총 어시스트 수
    
    // 챔피언 통계
    private String mostPlayedChampion; // 가장 많이 플레이한 챔피언
    
    /**
     * 🔧 편의 메서드: 승률 백분율 문자열
     */
    public String getWinRateString() {
        return String.format("%.1f%%", winRate);
    }
    
    /**
     * 🔧 편의 메서드: KDA 문자열  
     */
    public String getKDAString() {
        if (totalGames == 0) return "0.0";
        double avgKills = (double) totalKills / totalGames;
        double avgDeaths = (double) totalDeaths / totalGames;
        double avgAssists = (double) totalAssists / totalGames;
        return String.format("%.1f/%.1f/%.1f", avgKills, avgDeaths, avgAssists);
    }
    
    /**
     * 🔧 편의 메서드: 간단한 성과 평가
     */
    public String getPerformanceLevel() {
        if (winRate >= 70) return "매우 좋음";
        if (winRate >= 60) return "좋음"; 
        if (winRate >= 50) return "보통";
        if (winRate >= 40) return "아쉬움";
        return "분발 필요";
    }
}
