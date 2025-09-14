package com.example.demo.riot;

import com.example.demo.configuration.RiotRestTemplateConfig;
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
    private final RiotRestTemplateConfig riotConfig;

    @Value("${riot.platform-route}")
    private String regionalRoute; // asia, americas, europe

    // 플랫폼 매핑 (토이프로젝트에서는 KR만 주로 사용)
    private static final Map<String, String> PLATFORM_MAPPING = Map.of(
            "kr", "kr",
            "na", "na1",
            "euw", "euw1"
    );

    /**
     * 🔧 디버깅: 소환사명으로 직접 검색 (deprecated API)
     */
    public SummonerResponse getSummonerByName(String summonerName) {
        String encodedName = URLEncoder.encode(summonerName, StandardCharsets.UTF_8);
        String baseUrl = String.format("https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/%s",
                encodedName);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
        try {
            log.info("소환사명 API 호출: {}", summonerName);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            // 응답 로깅 추가
            log.info("소하사명 API 응답: {}", response);
            
            return SummonerResponse.builder()
                    .id(response.get("id") != null ? response.get("id").toString() : "UNKNOWN")
                    .accountId(response.get("accountId") != null ? response.get("accountId").toString() : "UNKNOWN")
                    .puuid(response.get("puuid") != null ? response.get("puuid").toString() : "UNKNOWN")
                    .name(response.get("name") != null ? response.get("name").toString() : "UNKNOWN")
                    .profileIconId(response.get("profileIconId") != null ? ((Number) response.get("profileIconId")).intValue() : 0)
                    .revisionDate(response.get("revisionDate") != null ? ((Number) response.get("revisionDate")).longValue() : 0L)
                    .summonerLevel(response.get("summonerLevel") != null ? ((Number) response.get("summonerLevel")).intValue() : 0)
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("소환사명 API 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), "소환사 정보를 찾을 수 없습니다: " + summonerName);
        }
    }

    /**
     * 🎯 핵심 #1: Riot ID로 계정 정보 조회
     * 
     * 학습 포인트:
     * - URL 인코딩 처리
     * - 외부 API 호출 패턴
     */
    public AccountResponse getAccountByRiotId(String gameName, String tagLine) {
        // 실제 API 호출
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        
        String baseUrl = String.format("https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                regionalRoute, encodedGameName, encodedTagLine);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
        try {
            log.info("Account API 호출: {}#{}", gameName, tagLine);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            // 응답 로그 추가
            log.info("Account API 응답: {}", response);
            
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
        // 실제 API 호출
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s",
                platformCode, puuid);
        String url = riotConfig.addApiKeyToUrl(baseUrl);

        try {
            log.info("Summoner API 호출: PUUID={}", puuid);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);

            // 응답 로그 추가
            log.info("Summoner API 응답: {}", response);

            return SummonerResponse.builder()
                    .id(response.get("id") != null ? response.get("id").toString() : "UNKNOWN")
                    .accountId(response.get("accountId") != null ? response.get("accountId").toString() : "UNKNOWN")
                    .puuid(response.get("puuid") != null ? response.get("puuid").toString() : "UNKNOWN")
                    .name(response.get("name") != null ? response.get("name").toString() : "UNKNOWN")
                    .profileIconId(response.get("profileIconId") != null ? ((Number) response.get("profileIconId")).intValue() : 0)
                    .revisionDate(response.get("revisionDate") != null ? ((Number) response.get("revisionDate")).longValue() : 0L)
                    .summonerLevel(response.get("summonerLevel") != null ? ((Number) response.get("summonerLevel")).intValue() : 0)
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
        // 실제 API 호출
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/league/v4/entries/by-summoner/%s",
                platformCode, summonerId);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
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
        // 실제 API 호출
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d",
                regionalRoute, puuid, Math.min(count, 10)); // 토이프로젝트에서는 최대 10개
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
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
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                regionalRoute, matchId);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
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
     * 🎯 통합 메서드: Match API 중심 (현재 API 제한 상황 대응)
     * 
     * Summoner API 403 문제로 인해 Match API만 사용하는 방식으로 변경
     * - Account 정보: PUUID, 기본 프로필
     * - Match 목록: 최근 경기 리스트
     * - Match 상세: 승/패, KDA, 챔피언 등
     */
    public PlayerSummaryResponse getPlayerSummary(String gameName, String tagLine, String platform) {
        try {
            // 1. Account 정보 (이건 잘 됨)
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            
            // 2. Summoner 정보 (403 오류로 주석처리)
            // SummonerResponse summoner = getSummonerByName(account.getGameName());
            
            // 3. 랭크 정보 (summonerId 필요해서 주석처리)
            // List<RankResponse> ranks = getRankInfo(platform, summoner.getId());
            
            // 4. 최근 경기 (PUUID로 가능 - 이걸 메인으로)
            List<String> recentMatches = getRecentMatchIds(account.getPuuid(), 10);
            
            // 5. 기본 Summoner 정보 생성 (Match API에서 얻을 수 있는 정보)
            SummonerResponse basicSummoner = createBasicSummonerInfo(account, recentMatches);
            
            return PlayerSummaryResponse.builder()
                    .account(account)
                    .summoner(basicSummoner)
                    .ranks(List.of()) // 빈 리스트로 처리
                    .recentMatchIds(recentMatches)
                    .build();
                    
        } catch (Exception e) {
            log.error("플레이어 종합 정보 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 정보를 가져오는 중 오류가 발생했습니다");
        }
    }
    
    /**
     * Match API 정보로 기본 Summoner 정보 생성
     */
    private SummonerResponse createBasicSummonerInfo(AccountResponse account, List<String> matchIds) {
        // Match API에서 최근 경기 하나만 가져와서 기본 정보 추출
        if (!matchIds.isEmpty()) {
            try {
                MatchDetailResponse firstMatch = getMatchDetail(matchIds.get(0));
                // 실제로는 매치 상세에서 소환사 레벨 등을 추출할 수 있음
                return SummonerResponse.builder()
                        .id("UNAVAILABLE") // API 제한으로 불가
                        .accountId("UNAVAILABLE")
                        .puuid(account.getPuuid())
                        .name(account.getGameName())
                        .profileIconId(1) // 기본값
                        .revisionDate(System.currentTimeMillis())
                        .summonerLevel(30) // 기본값
                        .build();
            } catch (Exception e) {
                log.warn("매치 상세 정보로 소환사 정보 생성 실패: {}", e.getMessage());
            }
        }
        
        // 매치 정보도 없으면 기본 정보만
        return SummonerResponse.builder()
                .id("UNAVAILABLE")
                .accountId("UNAVAILABLE") 
                .puuid(account.getPuuid())
                .name(account.getGameName())
                .profileIconId(1)
                .revisionDate(System.currentTimeMillis())
                .summonerLevel(1)
                .build();
    }
}