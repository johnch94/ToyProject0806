package com.example.demo.riot;

import com.example.demo.riot.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Riot API 서비스 (간소화 버전)
 * 
 * 토이프로젝트 목적:
 * - 외부 API 연동 학습
 * - DTO 패턴 실습
 * - 예외 처리 연습
 * 
 * 핵심 기능만 유지 (MVP)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiotApiService {

    private final RestTemplate riotRestTemplate;

    @Value("${riot.mock-mode:false}")
    private boolean mockMode;

    @Value("${riot.platform-route}")
    private String regionalRoute; // asia, americas, europe

    // 플랫폼 매핑 (토이프로젝트에서는 KR만 주로 사용)
    private static final Map<String, String> PLATFORM_MAPPING = Map.of(
            "kr", "kr",
            "na", "na1",
            "euw", "euw1"
    );

    /**
     * 🎯 핵심 #1: Riot ID로 계정 정보 조회
     * 
     * 학습 포인트:
     * - URL 인코딩 처리
     * - 외부 API 호출 패턴
     */
    public AccountResponse getAccountByRiotId(String gameName, String tagLine) {
        // Mock 모드: 개발용 가짜 데이터 반환
        if (mockMode) {
            log.info("Mock 모드: 가짜 Account 데이터 반환 - {}#{}", gameName, tagLine);
            return AccountResponse.builder()
                    .puuid("MOCK_PUUID_" + gameName)
                    .gameName(gameName)
                    .tagLine(tagLine)
                    .build();
        }
        
        // 실제 API 호출
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        
        String url = String.format("https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                regionalRoute, encodedGameName, encodedTagLine);
        
        try {
            log.info("Account API 호출: {}#{}", gameName, tagLine);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            return AccountResponse.builder()
                    .puuid(response.get("puuid").toString())
                    .gameName(response.get("gameName").toString())
                    .tagLine(response.get("tagLine").toString())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("Account API 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "Riot ID를 찾을 수 없습니다: " + gameName + "#" + tagLine);
        }
    }

    /**
     * 🎯 핵심 #2: 소환사 정보 조회
     */
    public SummonerResponse getSummonerByPuuid(String platform, String puuid) {
        // Mock 모드: 개발용 가짜 데이터
        if (mockMode) {
            log.info("Mock 모드: 가짜 Summoner 데이터 반환 - PUUID={}", puuid);
            return SummonerResponse.builder()
                    .id("MOCK_SUMMONER_ID")
                    .accountId("MOCK_ACCOUNT_ID")
                    .puuid(puuid)
                    .name("Mock Player")
                    .profileIconId(123)
                    .revisionDate(System.currentTimeMillis())
                    .summonerLevel(300)
                    .build();
        }
        
        // 실제 API 호출
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s",
                platformCode, puuid);
        
        try {
            log.info("Summoner API 호출: PUUID={}", puuid);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            return SummonerResponse.builder()
                    .id(response.get("id").toString())
                    .accountId(response.get("accountId").toString())
                    .puuid(response.get("puuid").toString())
                    .name(response.get("name").toString())
                    .profileIconId(((Number) response.get("profileIconId")).intValue())
                    .revisionDate(((Number) response.get("revisionDate")).longValue())
                    .summonerLevel(((Number) response.get("summonerLevel")).intValue())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("Summoner API 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), "소환사 정보를 찾을 수 없습니다");
        }
    }

    /**
     * 🎯 핵심 #3: 랭크 정보 조회
     * 
     * 비즈니스 로직:
     * - 솔로랭크, 자유랭크 구분
     * - 언랭크 처리
     */
    public List<RankResponse> getRankInfo(String platform, String summonerId) {
        // Mock 모드: 개발용 가짜 랭크 데이터
        if (mockMode) {
            log.info("Mock 모드: 가짜 랭크 데이터 반환 - SummonerID={}", summonerId);
            return List.of(
                    RankResponse.builder()
                            .queueType("RANKED_SOLO_5x5")
                            .tier("CHALLENGER")
                            .rank("I")
                            .leaguePoints(1337)
                            .wins(150)
                            .losses(50)
                            .build(),
                    RankResponse.builder()
                            .queueType("RANKED_FLEX_SR")
                            .tier("DIAMOND")
                            .rank("II")
                            .leaguePoints(67)
                            .wins(80)
                            .losses(45)
                            .build()
            );
        }
        
        // 실제 API 호출
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/league/v4/entries/by-summoner/%s",
                platformCode, summonerId);
        
        try {
            log.info("League API 호출: SummonerID={}", summonerId);
            List<Map<String, Object>> response = riotRestTemplate.getForObject(url, List.class);
            
            if (response == null || response.isEmpty()) {
                return List.of(); // 언랭크
            }
            
            return response.stream()
                    .map(entry -> RankResponse.builder()
                            .queueType(entry.get("queueType").toString())
                            .tier(entry.get("tier") != null ? entry.get("tier").toString() : "UNRANKED")
                            .rank(entry.get("rank") != null ? entry.get("rank").toString() : "")
                            .leaguePoints(entry.get("leaguePoints") != null ? 
                                    ((Number) entry.get("leaguePoints")).intValue() : 0)
                            .wins(entry.get("wins") != null ? 
                                    ((Number) entry.get("wins")).intValue() : 0)
                            .losses(entry.get("losses") != null ? 
                                    ((Number) entry.get("losses")).intValue() : 0)
                            .build())
                    .toList();
                    
        } catch (HttpClientErrorException e) {
            log.warn("League API 실패 (언랭크일 수 있음): {}", e.getMessage());
            return List.of(); // 랭크 정보 없음
        }
    }

    /**
     * 🎯 핵심 #4: 최근 경기 목록 조회
     */
    public List<String> getRecentMatchIds(String puuid, int count) {
        // Mock 모드: 개발용 가짜 경기 ID 데이터
        if (mockMode) {
            log.info("Mock 모드: 가짜 경기 ID 데이터 반환 - PUUID={}, count={}", puuid, count);
            return List.of(
                    "KR_123456789_MOCK_MATCH_1",
                    "KR_123456789_MOCK_MATCH_2", 
                    "KR_123456789_MOCK_MATCH_3",
                    "KR_123456789_MOCK_MATCH_4",
                    "KR_123456789_MOCK_MATCH_5"
            ).subList(0, Math.min(count, 5));
        }
        
        // 실제 API 호출
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d",
                regionalRoute, puuid, Math.min(count, 10)); // 토이프로젝트에서는 최대 10개
        
        try {
            log.info("Match API 호출: PUUID={}, count={}", puuid, count);
            List<String> matchIds = riotRestTemplate.getForObject(url, List.class);
            return matchIds != null ? matchIds : List.of();
            
        } catch (HttpClientErrorException e) {
            log.error("Match API 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), "경기 목록을 가져올 수 없습니다");
        }
    }

    /**
     * 🎯 핵심 #5: 경기 상세 정보 조회
     */
    public MatchDetailResponse getMatchDetail(String matchId) {
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                regionalRoute, matchId);
        
        try {
            log.info("Match Detail API 호출: MatchID={}", matchId);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            Map<String, Object> info = (Map<String, Object>) response.get("info");
            List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");
            
            return MatchDetailResponse.builder()
                    .matchId(matchId)
                    .gameMode(info.get("gameMode").toString())
                    .gameDuration(((Number) info.get("gameDuration")).longValue())
                    .gameCreation(((Number) info.get("gameCreation")).longValue())
                    .participantCount(participants.size())
                    .queueId(((Number) info.get("queueId")).intValue())
                    .mapId(((Number) info.get("mapId")).intValue())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("Match Detail API 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "경기 상세 정보를 가져올 수 없습니다: " + matchId);
        }
    }

    /**
     * 🎯 통합 메서드: MVP의 핵심 (플레이어 종합 정보)
     * 
     * 한 번의 호출로 프론트엔드에서 필요한 모든 정보 제공
     */
    public PlayerSummaryResponse getPlayerSummary(String gameName, String tagLine, String platform) {
        try {
            // 1. Account 정보
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            
            // 2. Summoner 정보  
            SummonerResponse summoner = getSummonerByPuuid(platform, account.getPuuid());
            
            // 3. 랭크 정보
            List<RankResponse> ranks = getRankInfo(platform, summoner.getId());
            
            // 4. 최근 경기 (5경기만)
            List<String> recentMatches = getRecentMatchIds(account.getPuuid(), 5);
            
            return PlayerSummaryResponse.builder()
                    .account(account)
                    .summoner(summoner)
                    .ranks(ranks)
                    .recentMatchIds(recentMatches)
                    // .topChampions() 제거됨 - DTO에서 필드 삭제
                    .build();
                    
        } catch (Exception e) {
            log.error("플레이어 종합 정보 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 정보를 가져오는 중 오류가 발생했습니다");
        }
    }

    /*
     * 🗑️ 제거된 메서드들 (토이프로젝트에는 과함):
     * - getChampionMastery(): 챔피언 숙련도 상세 분석
     * - 복잡한 매치 통계 분석
     * - 다중 지역 지원 확장
     * 
     * 💡 학습 완료 후 추가 고려사항:
     * - Redis 캐싱 적용
     * - Rate Limit 관리
     * - Circuit Breaker 패턴
     */
}
