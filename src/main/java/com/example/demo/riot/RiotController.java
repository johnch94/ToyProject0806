package com.example.demo.riot;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.riot.dto.PlayerMatchHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 🎮 Riot API 컨트롤러 - 핵심 기능만
 * 
 * 단 1개 기능: 플레이어 전적 조회
 * - 불필요한 테스트 엔드포인트 모두 제거
 * - 중복 기능 모두 제거
 * - 핵심만 남김
 */
@RestController
@RequestMapping("/api/riot")
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
}
