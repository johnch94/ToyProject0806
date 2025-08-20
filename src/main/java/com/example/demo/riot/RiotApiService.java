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
 * League of Legends API 서비스
 * 롤 게임 데이터 조회를 위한 Riot API 연동
 * 
 * 토이프로젝트용 MVP 구현:
 * - 플레이어 기본 정보 조회 (계정, 소환사, 랭크)
 * - 최근 경기 목록 및 상세 정보
 * - 챔피언 숙련도 정보
 * 
 * 실제 서비스에서는 캐싱, Rate Limit 처리, 장애 복구 등이 추가로 필요
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiotApiService {

    private final RestTemplate riotRestTemplate;

    @Value("${riot.platform-route}")
    private String regionalRoute; // asia, americas, europe

    // 플랫폼별 매핑 (소환사 API용)
    private static final Map<String, String> PLATFORM_MAPPING = Map.of(
            "kr", "kr",
            "na", "na1",
            "euw", "euw1",
            "eune", "eun1",
            "jp", "jp1"
    );

    /**
     * 1. Account API - Riot ID로 계정 정보 조회
     */
    public AccountResponse getAccountByRiotId(String gameName, String tagLine) {
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        
        String url = String.format("https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                regionalRoute, encodedGameName, encodedTagLine);
        
        try {
            log.info("Fetching account by Riot ID: {}#{}", gameName, tagLine);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            return AccountResponse.builder()
                    .puuid(response.get("puuid").toString())
                    .gameName(response.get("gameName").toString())
                    .tagLine(response.get("tagLine").toString())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("Account API 호출 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "Riot ID를 찾을 수 없습니다: " + gameName + "#" + tagLine);
        }
    }

    /**
     * 2. Summoner API - 소환사 정보 조회 (PUUID로)
     */
    public SummonerResponse getSummonerByPuuid(String platform, String puuid) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s",
                platformCode, puuid);
        
        try {
            log.info("Fetching summoner by PUUID: {} on platform: {}", puuid, platform);
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
            log.error("Summoner API 호출 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "소환사 정보를 찾을 수 없습니다");
        }
    }

    /**
     * 3. Match API - 최근 경기 ID 목록 조회
     */
    public List<String> getRecentMatchIds(String puuid, int count) {
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d",
                regionalRoute, puuid, Math.min(count, 20)); // 최대 20개 제한
        
        try {
            log.info("Fetching {} recent matches for PUUID: {}", count, puuid);
            List<String> matchIds = riotRestTemplate.getForObject(url, List.class);
            return matchIds != null ? matchIds : List.of();
            
        } catch (HttpClientErrorException e) {
            log.error("Match IDs API 호출 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "최근 경기 목록을 가져올 수 없습니다");
        }
    }

    /**
     * 4. Match API - 특정 경기 상세 정보 조회
     */
    public MatchDetailResponse getMatchDetail(String matchId) {
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                regionalRoute, matchId);
        
        try {
            log.info("Fetching match detail: {}", matchId);
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
            log.error("Match Detail API 호출 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "경기 상세 정보를 가져올 수 없습니다: " + matchId);
        }
    }

    /**
     * 5. League API - 랭크 정보 조회
     */
    public List<RankResponse> getRankInfo(String platform, String summonerId) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/league/v4/entries/by-summoner/%s",
                platformCode, summonerId);
        
        try {
            log.info("Fetching rank info for summoner: {} on platform: {}", summonerId, platform);
            List<Map<String, Object>> response = riotRestTemplate.getForObject(url, List.class);
            
            if (response == null || response.isEmpty()) {
                return List.of();
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
            log.error("League API 호출 실패: {}", e.getMessage());
            // 랭크 정보는 없을 수도 있으므로 빈 리스트 반환
            return List.of();
        }
    }

    /**
     * 6. Champion Mastery API - 챔피언 숙련도 조회
     */
    public List<ChampionMasteryResponse> getChampionMastery(String platform, String puuid, int count) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/%s/top?count=%d",
                platformCode, puuid, Math.min(count, 10));
        
        try {
            log.info("Fetching top {} champion masteries for PUUID: {}", count, puuid);
            List<Map<String, Object>> response = riotRestTemplate.getForObject(url, List.class);
            
            if (response == null || response.isEmpty()) {
                return List.of();
            }
            
            return response.stream()
                    .map(mastery -> ChampionMasteryResponse.builder()
                            .championId(((Number) mastery.get("championId")).intValue())
                            .championLevel(((Number) mastery.get("championLevel")).intValue())
                            .championPoints(((Number) mastery.get("championPoints")).intValue())
                            .lastPlayTime(((Number) mastery.get("lastPlayTime")).longValue())
                            .championPointsSinceLastLevel(((Number) mastery.get("championPointsSinceLastLevel")).intValue())
                            .championPointsUntilNextLevel(((Number) mastery.get("championPointsUntilNextLevel")).intValue())
                            .build())
                    .toList();
                    
        } catch (HttpClientErrorException e) {
            log.error("Champion Mastery API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 통합 조회 메서드: Riot ID로 모든 정보 한 번에 조회
     */
    public PlayerSummaryResponse getPlayerSummary(String gameName, String tagLine, String platform) {
        try {
            // 1. Account 정보
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            
            // 2. Summoner 정보
            SummonerResponse summoner = getSummonerByPuuid(platform, account.getPuuid());
            
            // 3. 랭크 정보
            List<RankResponse> ranks = getRankInfo(platform, summoner.getId());
            
            // 4. 최근 경기 (5경기)
            List<String> recentMatches = getRecentMatchIds(account.getPuuid(), 5);
            
            // 5. 챔피언 숙련도 (Top 3)
            List<ChampionMasteryResponse> topChampions = getChampionMastery(platform, account.getPuuid(), 3);
            
            return PlayerSummaryResponse.builder()
                    .account(account)
                    .summoner(summoner)
                    .ranks(ranks)
                    .recentMatchIds(recentMatches)
                    .topChampions(topChampions)
                    .build();
                    
        } catch (Exception e) {
            log.error("플레이어 종합 정보 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 정보를 가져오는 중 오류가 발생했습니다");
        }
    }
}
