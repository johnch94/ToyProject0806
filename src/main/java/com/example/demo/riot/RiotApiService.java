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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🎮 Riot API 서비스 - 핵심 기능만
 * 
 * 3개 핵심 메서드:
 * 1. getPlayerMatchHistory() - 통합 전적 조회 (메인)
 * 2. getAccountByRiotId() - 플레이어 기본 정보
 * 3. getMatchDetail() - 경기별 상세 전적 (진짜 전적 데이터!)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiotApiService {

    private final RestTemplate riotRestTemplate;
    private final RiotRestTemplateConfig riotConfig;

    @Value("${riot.platform-route}")
    private String regionalRoute; // asia

    /**
     * 🎯 메인 메서드: 플레이어 완전한 전적 조회
     * 
     * 동작:
     * 1. 플레이어 기본 정보 조회 (PUUID 획득)
     * 2. 최근 경기 ID 목록 조회
     * 3. 각 경기의 상세 전적 조회 (진짜 전적!)
     * 4. 통계 계산 (승률, 평균 KDA 등)
     */
    public PlayerMatchHistoryResponse getPlayerMatchHistory(String gameName, String tagLine, int count) {
        try {
            // 1. 플레이어 기본 정보
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            log.info("플레이어 정보 획득: PUUID={}", account.getPuuid());
            
            // 2. 최근 경기 ID 목록
            List<String> matchIds = getRecentMatchIds(account.getPuuid(), count);
            log.info("경기 ID {} 개 조회 완료", matchIds.size());
            
            // 3. 🔥 핵심: 각 경기의 실제 전적 조회
            List<MatchDetailResponse> matches = matchIds.stream()
                    .map(matchId -> getMatchDetail(matchId, account.getPuuid()))
                    .collect(Collectors.toList());
            log.info("상세 전적 {} 경기 분석 완료", matches.size());
            
            // 4. 통계 계산
            MatchStatsResponse stats = calculateMatchStats(matches);
            
            return PlayerMatchHistoryResponse.builder()
                    .player(account)
                    .matches(matches)
                    .stats(stats)
                    .build();
                    
        } catch (Exception e) {
            log.error("플레이어 전적 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "전적을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 🔧 헬퍼: Riot ID로 계정 정보 조회
     */
    public AccountResponse getAccountByRiotId(String gameName, String tagLine) {
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        
        String baseUrl = String.format("https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                regionalRoute, encodedGameName, encodedTagLine);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
        try {
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            return AccountResponse.builder()
                    .puuid(response.get("puuid").toString())
                    .gameName(response.get("gameName").toString())
                    .tagLine(response.get("tagLine").toString())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), 
                    "플레이어를 찾을 수 없습니다: " + gameName + "#" + tagLine);
        }
    }

    /**
     * 🔧 헬퍼: 최근 경기 ID 목록 조회
     */
    private List<String> getRecentMatchIds(String puuid, int count) {
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d",
                regionalRoute, puuid, count);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
        try {
            List<String> matchIds = riotRestTemplate.getForObject(url, List.class);
            return matchIds != null ? matchIds : List.of();
            
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "경기 목록을 가져올 수 없습니다");
        }
    }

    /**
     * 🎯 핵심: 경기 상세 정보 조회 - 진짜 전적 데이터 추출!
     * 
     * 기존 문제: 의미없는 queueId, mapId만 추출
     * 개선: 실제 게임 성과 데이터 추출 (승부, 챔피언, KDA, CS 등)
     */
    public MatchDetailResponse getMatchDetail(String matchId, String targetPuuid) {
        String baseUrl = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                regionalRoute, matchId);
        String url = riotConfig.addApiKeyToUrl(baseUrl);
        
        try {
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            Map<String, Object> info = (Map<String, Object>) response.get("info");
            List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");
            
            // 🔥 핵심: 타겟 플레이어 찾기
            Map<String, Object> targetPlayer = participants.stream()
                    .filter(p -> targetPuuid.equals(p.get("puuid").toString()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("플레이어 데이터를 찾을 수 없습니다"));
            
            // 🔥 핵심: 실제 전적 데이터 추출
            return MatchDetailResponse.builder()
                    .matchId(matchId)
                    .championName(getChampionName((Integer) targetPlayer.get("championId")))
                    .victory((Boolean) targetPlayer.get("win"))
                    .kills((Integer) targetPlayer.get("kills"))
                    .deaths((Integer) targetPlayer.get("deaths"))
                    .assists((Integer) targetPlayer.get("assists"))
                    .cs(((Integer) targetPlayer.get("totalMinionsKilled")) + 
                        ((Integer) targetPlayer.get("neutralMinionsKilled")))
                    .totalDamage((Integer) targetPlayer.get("totalDamageDealtToChampions"))
                    .goldEarned((Integer) targetPlayer.get("goldEarned"))
                    .gameLength(((Number) info.get("gameDuration")).longValue())
                    .gameDate(Instant.ofEpochMilli(((Number) info.get("gameCreation")).longValue())
                            .atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .queueType(getQueueTypeName((Integer) info.get("queueId")))
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("경기 상세 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "경기 상세 정보를 가져올 수 없습니다: " + matchId);
        }
    }

    /**
     * 🔧 헬퍼: 여러 경기 통계 계산
     */
    private MatchStatsResponse calculateMatchStats(List<MatchDetailResponse> matches) {
        if (matches.isEmpty()) {
            return MatchStatsResponse.builder()
                    .totalGames(0)
                    .wins(0)
                    .losses(0)
                    .winRate(0.0)
                    .averageKDA(0.0)
                    .mostPlayedChampion("없음")
                    .build();
        }

        int wins = (int) matches.stream().mapToInt(m -> m.isVictory() ? 1 : 0).sum();
        int totalKills = matches.stream().mapToInt(MatchDetailResponse::getKills).sum();
        int totalDeaths = matches.stream().mapToInt(MatchDetailResponse::getDeaths).sum();
        int totalAssists = matches.stream().mapToInt(MatchDetailResponse::getAssists).sum();
        
        // 가장 많이 플레이한 챔피언
        String mostPlayedChampion = matches.stream()
                .collect(Collectors.groupingBy(MatchDetailResponse::getChampionName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("알 수 없음");

        return MatchStatsResponse.builder()
                .totalGames(matches.size())
                .wins(wins)
                .losses(matches.size() - wins)
                .winRate(matches.size() > 0 ? (double) wins / matches.size() * 100 : 0.0)
                .averageKDA(totalDeaths > 0 ? (double) (totalKills + totalAssists) / totalDeaths : 
                           (double) (totalKills + totalAssists))
                .mostPlayedChampion(mostPlayedChampion)
                .totalKills(totalKills)
                .totalDeaths(totalDeaths)
                .totalAssists(totalAssists)
                .build();
    }

    /**
     * 🔧 헬퍼: 챔피언 ID를 이름으로 변환 (간단 버전)
     */
    private String getChampionName(Integer championId) {
        // 토이프로젝트용 간단한 매핑 (실제로는 Data Dragon API 사용)
        // Map.of()는 최대 10개까지만 지원하므로 HashMap 사용
        Map<Integer, String> champions = new java.util.HashMap<>();
        champions.put(1, "애니");
        champions.put(2, "올라프");
        champions.put(3, "갈리오");
        champions.put(4, "트위스티드 페이트");
        champions.put(5, "신 짜오");
        champions.put(10, "케이틀린");
        champions.put(11, "마스터 이");
        champions.put(12, "알리스타");
        champions.put(13, "라이즈");
        champions.put(14, "사이온");
        champions.put(17, "티모");
        champions.put(18, "트리스타나");
        champions.put(19, "워윅");
        champions.put(20, "누누와 윌럼프");
        champions.put(21, "미스 포츈");
        champions.put(22, "애쉬");
        champions.put(23, "트린다미어");
        champions.put(24, "잭스");
        champions.put(25, "모르가나");
        champions.put(26, "질리언");
        champions.put(103, "아리");
        champions.put(238, "제드");
        champions.put(157, "야스오");
        champions.put(84, "아칼리");
        champions.put(268, "아지르");
        
        return champions.getOrDefault(championId, "챔피언 " + championId);
    }

    /**
     * 🔧 헬퍼: 큐 타입 ID를 이름으로 변환
     */
    private String getQueueTypeName(Integer queueId) {
        Map<Integer, String> queues = Map.of(
                420, "솔로랭크",
                440, "자유랭크",
                450, "무작위 총력전",
                400, "일반 게임",
                830, "AI 상대"
        );
        return queues.getOrDefault(queueId, "기타 게임");
    }
}
