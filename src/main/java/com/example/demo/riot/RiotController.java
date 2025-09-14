package com.example.demo.riot;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.riot.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Riot API 컨트롤러 (통합 버전)
 * 
 * 토이프로젝트 MVP 기능:
 * 1. 플레이어 검색 (핵심)
 * 2. 랭크 조회 (필수) 
 * 3. 최근 경기 (추가 가치)
 * 
 * 복잡한 분석 기능은 제거하고 학습 목적에 맞는 기본 기능만 유지
 */
@RestController
@RequestMapping("/api/riot")
@RequiredArgsConstructor
@Slf4j
public class RiotController {

    private final RiotApiService riotApiService;

    /**
     * 🔧 소환사명으로 직접 검색 테스트 (deprecated API)
     */
    @GetMapping("/test/by-name")
    public ApiResponse<SummonerResponse> testByName(
            @RequestParam String summonerName) {
        
        try {
            SummonerResponse summoner = riotApiService.getSummonerByName(summonerName);
            return ApiResponse.<SummonerResponse>builder()
                    .success(true)
                    .message("소환사명 API 성공")
                    .data(summoner)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<SummonerResponse>builder()
                    .success(false)
                    .message("에러: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    /**
     * 🔧 Summoner API만 테스트
     */
    @GetMapping("/test/summoner")
    public ApiResponse<SummonerResponse> testSummoner(
            @RequestParam String puuid,
            @RequestParam(defaultValue = "kr") String platform) {
        
        try {
            SummonerResponse summoner = riotApiService.getSummonerByPuuid(platform, puuid);
            return ApiResponse.<SummonerResponse>builder()
                    .success(true)
                    .message("Summoner API 성공")
                    .data(summoner)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<SummonerResponse>builder()
                    .success(false)
                    .message("에러: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    /**
     * 🔧 Account API만 테스트
     */
    @GetMapping("/test/account")
    public ApiResponse<AccountResponse> testAccount(
            @RequestParam String gameName,
            @RequestParam String tagLine) {
        
        try {
            AccountResponse account = riotApiService.getAccountByRiotId(gameName, tagLine);
            return ApiResponse.<AccountResponse>builder()
                    .success(true)
                    .message("Account API 성공")
                    .data(account)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AccountResponse>builder()
                    .success(false)
                    .message("에러: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    /**
     * 🎯 MVP #1: 플레이어 검색 (가장 중요)
     * - 롤 유저들이 가장 많이 사용하는 기능
     * - 한 번의 호출로 모든 기본 정보 제공
     * - 프론트엔드 개발 편의성 극대화
     */
    @GetMapping("/player")
    public ApiResponse<PlayerSummaryResponse> getPlayer(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "kr") String platform) {
        
        log.info("플레이어 검색: {}#{} ({})", gameName, tagLine, platform);
        
        PlayerSummaryResponse player = riotApiService.getPlayerSummary(gameName, tagLine, platform);
        
        return ApiResponse.<PlayerSummaryResponse>builder()
                .success(true)
                .message("플레이어 정보 조회 성공")
                .data(player)
                .build();
    }

    /**
     * 🎯 MVP #2: 간단한 플레이어 정보 (빠른 검색용)
     * 
     * 사용법: /api/riot/player/simple?gameName=Faker&tagLine=KR1
     * 
     * 실제 서비스에서는?
     * - 자동완성 검색에 활용
     * - 모바일에서 빠른 응답 필요시 사용
     */
    @GetMapping("/player/simple")
    public ApiResponse<SimplePlayerResponse> getSimplePlayer(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "kr") String platform) {
        
        log.info("간단한 플레이어 검색: {}#{}", gameName, tagLine);
        
        // 핵심 정보만 조회
        AccountResponse account = riotApiService.getAccountByRiotId(gameName, tagLine);
        SummonerResponse summoner = riotApiService.getSummonerByPuuid(platform, account.getPuuid());
        List<RankResponse> ranks =
                (summoner.getId() != null && !"UNKNOWN".equals(summoner.getId()))
                        ? riotApiService.getRankInfo(platform, summoner.getId())
                        : List.of();
        
        SimplePlayerResponse simple = SimplePlayerResponse.builder()
                .gameName(account.getGameName())
                .tagLine(account.getTagLine())
                .summonerName(account.getGameName() + "#" + account.getTagLine()) // gameName#tagLine 형식
                .summonerLevel(summoner.getSummonerLevel())
                .profileIconId(summoner.getProfileIconId())
                .soloRank(ranks.stream()
                        .filter(r -> "RANKED_SOLO_5x5".equals(r.getQueueType()))
                        .findFirst()
                        .map(r -> r.getTier() + " " + r.getRank())
                        .orElse("UNRANKED"))
                .build();
        
        return ApiResponse.<SimplePlayerResponse>builder()
                .success(true)
                .message("플레이어 기본 정보 조회 성공")
                .data(simple)
                .build();
    }

    /**
     * 🎯 MVP #3: 최근 경기 목록 (학습용)
     * 
     * 사용법: /api/riot/matches?puuid=abc123&count=5
     * 
     * 학습 포인트:
     * - 외부 API 호출 방법
     * - 에러 처리 패턴
     */
    @GetMapping("/matches")
    public ApiResponse<List<String>> getRecentMatches(
            @RequestParam String puuid,
            @RequestParam(defaultValue = "5") int count) {
        
        log.info("최근 경기 목록 조회: {} ({}경기)", puuid, count);
        
        List<String> matchIds = riotApiService.getRecentMatchIds(puuid, count);
        
        return ApiResponse.<List<String>>builder()
                .success(true)
                .message(String.format("최근 %d경기 조회 성공", matchIds.size()))
                .data(matchIds)
                .build();
    }

    /**
     * 🎯 추가 기능: 경기 상세 (나중에 확장 가능)
     * 
     * 현재는 기본 정보만, 향후 상세 분석 기능 추가 예정
     */
    @GetMapping("/match/{matchId}")
    public ApiResponse<MatchDetailResponse> getMatchDetail(@PathVariable String matchId) {
        
        log.info("경기 상세 정보 조회: {}", matchId);
        
        MatchDetailResponse matchDetail = riotApiService.getMatchDetail(matchId);
        
        return ApiResponse.<MatchDetailResponse>builder()
                .success(true)
                .message("경기 상세 정보 조회 성공")
                .data(matchDetail)
                .build();
    }

    /* 
     * 🗑️ 제거된 기능들 (토이프로젝트에는 과한 기능):
     * - 복잡한 통계 분석
     * - 다중 플랫폼 지원
     * - 챔피언 숙련도 상세 분석
     * - 매치 히스토리 필터링
     * 
     * 💡 나중에 추가 가능:
     * - 승률 분석 차트
     * - 플레이 스타일 분석
     * - 친구 추천 시스템
     */
}
