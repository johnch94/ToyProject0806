package com.example.demo.riot;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.riot.dto.MatchDetailWithPlayerResponse;
import com.example.demo.riot.dto.PlayerMatchHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 🎮 Riot API 컨트롤러
 * 
 * 2개 핵심 기능:
 * 1. 플레이어 전적 조회 (검색 + 리스트)
 * 2. 단일 경기 상세 조회 (상세 페이지용)
 */
@RestController
@RequestMapping("/api/riot")
@CrossOrigin(origins = "*")  // CORS 허용
@RequiredArgsConstructor
@Slf4j
public class RiotController {

    private final RiotApiService riotApiService;

    /**
     * 🎯 유일한 핵심 기능: 플레이어 전적 조회
     * 
     * 사용법: GET /api/riot/player/Faker/KR1/matches?count=5
     * 
     * 반환값:
     * - 플레이어 기본 정보
     * - 최근 N경기의 상세 전적 (승부, 챔피언, KDA, CS 등)
     * - 통계 요약 (승률, 평균 KDA, 주력 챔피언)
     */
    @GetMapping("/player/{gameName}/{tagLine}/matches")
    public ApiResponse<PlayerMatchHistoryResponse> getPlayerMatches(
            @PathVariable String gameName,
            @PathVariable String tagLine,
            @RequestParam(defaultValue = "5") int count) {
        
        log.info("플레이어 전적 조회: {}#{}, {}경기", gameName, tagLine, count);
        
        PlayerMatchHistoryResponse matchHistory = riotApiService.getPlayerMatchHistory(
                gameName, tagLine, Math.min(count, 10)); // 최대 10경기로 제한
        
        return ApiResponse.<PlayerMatchHistoryResponse>builder()
                .success(true)
                .message(String.format("%s#%s의 최근 %d경기 전적", gameName, tagLine, matchHistory.getMatches().size()))
                .data(matchHistory)
                .build();
    }

    /**
     * 🎯 신규 기능: 단일 경기 상세 조회
     * 
     * 사용법: GET /api/riot/match/KR_12345?puuid=abc...xyz
     * 
     * 용도:
     * - 프론트엔드에서 경기 클릭 시 상세 페이지 표시
     * - URL로 직접 접근 가능
     * - 새로고침해도 데이터 유지
     * 
     * @param matchId Riot Match ID (예: KR_7215169365_1)
     * @param puuid 플레이어 PUUID (어느 플레이어 시점인지)
     * @return 해당 플레이어의 경기 상세 정보
     */
    @GetMapping("/match/{matchId}")
    public ApiResponse<MatchDetailWithPlayerResponse> getMatchDetail(
            @PathVariable String matchId,
            @RequestParam String puuid) {
        
        log.info("단일 경기 조회: matchId={}, puuid={}", matchId, puuid);
        
        MatchDetailWithPlayerResponse matchDetail = riotApiService.getMatchDetailWithPlayer(matchId, puuid);
        
        return ApiResponse.<MatchDetailWithPlayerResponse>builder()
                .success(true)
                .message(String.format("%s님의 %s 경기 상세 정보", 
                        matchDetail.getPlayerDisplayName(), 
                        matchDetail.isVictory() ? "승리" : "패배"))
                .data(matchDetail)
                .build();
    }
}
