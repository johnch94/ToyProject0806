package com.example.demo.riot.dto;

import lombok.*;
import java.util.List;

/**
 * 🎮 플레이어 완전한 전적 응답 DTO
 * 
 * 3가지 핵심 정보를 통합:
 * 1. 플레이어 기본 정보 (누구인가?)
 * 2. 경기별 상세 전적 (어떻게 플레이했나?)
 * 3. 통계 요약 (전체적인 실력은?)
 * 
 * 이 하나의 응답으로 모든 전적 분석 가능!
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerMatchHistoryResponse {
    
    // 플레이어 기본 정보
    private AccountResponse player;
    
    // 🔥 핵심: 경기별 상세 전적 (실제 게임 성과 데이터!)
    private List<MatchDetailResponse> matches;
    
    // 🔥 핵심: 통계 요약 (승률, 평균 KDA, 주력 챔피언 등)
    private MatchStatsResponse stats;
    
    /**
     * 🔧 편의 메서드: 플레이어 표시명
     */
    public String getPlayerDisplayName() {
        return player.getGameName() + "#" + player.getTagLine();
    }
    
    /**
     * 🔧 편의 메서드: 간단한 요약 텍스트 (스토리 생성 기초)
     */
    public String getSummaryText() {
        return String.format("%s님의 최근 %d경기: %s승 %s패 (승률 %s), 주력 챔피언: %s",
                getPlayerDisplayName(),
                stats.getTotalGames(),
                stats.getWins(),
                stats.getLosses(), 
                stats.getWinRateString(),
                stats.getMostPlayedChampion());
    }
}
