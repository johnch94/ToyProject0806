package com.example.demo.riot;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.riot.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Riot API 컨트롤러
 * League of Legends 게임 데이터 조회 API
 * 
 * 토이프로젝트 목적:
 * - 롤 API 연동 학습
 * - RESTful API 설계 연습
 * - 외부 API 호출 및 예외처리 학습
 * 
 * 주요 기능: 플레이어 검색, 랭크 조회, 경기 내역 확인
 */
@RestController
@RequestMapping("/api/riot")
@RequiredArgsConstructor
@Slf4j
public class RiotApiController {

    private final RiotApiService riotApiService;

    /**
     * 플레이어 종합 정보 조회 (MVP의 핵심 API)
     * 
     * 왜 이 방식인가?
     * - 한 번의 호출로 필요한 정보를 모두 가져와 프론트엔드 개발 편의성 증대
     * - 여러 API 호출을 서버에서 처리해 네트워크 비용 절약
     * 
     * 실제 서비스에서는?
     * - 캐싱 적용으로 응답 속도 개선
     * - 부분 실패 시 일부 데이터라도 반환하는 Circuit Breaker 패턴 적용
     */
    @GetMapping("/player/summary")
    public ApiResponse<PlayerSummaryResponse> getPlayerSummary(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "kr") String platform) {
        
        log.info("플레이어 종합 정보 조회: {}#{} ({})", gameName, tagLine, platform);
        
        PlayerSummaryResponse summary = riotApiService.getPlayerSummary(gameName, tagLine, platform);
        
        return ApiResponse.<PlayerSummaryResponse>builder()
                .success(true)
                .message("플레이어 정보 조회 성공")
                .data(summary)
                .build();
    }

    /**
     * 간단한 플레이어 정보 (모바일/간단한 검색용)
     * 
     * MVP 우선순위: 높음
     * - 빠른 검색 결과 제공
     * - 최소한의 네트워크 사용
     */
    @GetMapping("/player/simple")
    public ApiResponse<SimplePlayerResponse> getSimplePlayer(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "kr") String platform) {
        
        log.info("간단한 플레이어 정보 조회: {}#{} ({})", gameName, tagLine, platform);
        
        // 필수 정보만 조회: Account + Summoner + 솔로랭크
        AccountResponse account = riotApiService.getAccountByRiotId(gameName, tagLine);
        SummonerResponse summoner = riotApiService.getSummonerByPuuid(platform, account.getPuuid());
        List<RankResponse> ranks = riotApiService.getRankInfo(platform, summoner.getId());
        
        SimplePlayerResponse simple = SimplePlayerResponse.builder()
                .gameName(account.getGameName())
                .tagLine(account.getTagLine())
                .summonerName(summoner.getName())
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
     * 최근 경기 목록 조회
     * 
     * 학습 포인트:
     * - 페이징 처리 (count 파라미터)
     * - 외부 API Rate Limit 고려
     */
    @GetMapping("/matches/recent")
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
     * 경기 상세 정보 조회
     * 
     * 추후 확장 가능:
     * - 참가자별 통계 분석
     * - 아이템 빌드 정보
     * - 팀 조합 분석
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

    /**
     * 랭크 정보 조회
     * 
     * 비즈니스 로직:
     * - 솔로랭크, 자유랭크 분리 표시
     * - 승률 자동 계산 (DTO에서 처리)
     */
    @GetMapping("/rank")
    public ApiResponse<List<RankResponse>> getRank(
            @RequestParam String summonerId,
            @RequestParam(defaultValue = "kr") String platform) {
        
        log.info("랭크 정보 조회: {} ({})", summonerId, platform);
        
        List<RankResponse> ranks = riotApiService.getRankInfo(platform, summonerId);
        
        return ApiResponse.<List<RankResponse>>builder()
                .success(true)
                .message("랭크 정보 조회 성공")
                .data(ranks)
                .build();
    }

    /**
     * 챔피언 숙련도 조회
     * 
     * 게임적 가치:
     * - 플레이어의 주력 챔피언 파악
     * - 숙련도 레벨을 통한 실력 추정
     */
    @GetMapping("/mastery")
    public ApiResponse<List<ChampionMasteryResponse>> getChampionMastery(
            @RequestParam String puuid,
            @RequestParam(defaultValue = "kr") String platform,
            @RequestParam(defaultValue = "3") int count) {
        
        log.info("챔피언 숙련도 조회: {} (상위 {}개)", puuid, count);
        
        List<ChampionMasteryResponse> masteries = riotApiService.getChampionMastery(platform, puuid, count);
        
        return ApiResponse.<List<ChampionMasteryResponse>>builder()
                .success(true)
                .message(String.format("상위 %d 챔피언 숙련도 조회 성공", masteries.size()))
                .data(masteries)
                .build();
    }
}
