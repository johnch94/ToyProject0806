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
 * 🎮 League of Legends API 서비스 (에러 수정 완료)
 * 
 * 토이프로젝트용 MVP 구현:
 * - 플레이어 기본 정보 조회 (계정, 소환사, 랭크)
 * - 최근 경기 목록 및 상세 정보
 * - 챔피언 숙련도 정보
 * 
 * 주요 개선사항:
 * - 안전한 조회 메서드들 추가 (부분 실패 허용)
 * - 새로운 PlayerResponse DTO 지원
 * - 기존 호환성 유지 (@Deprecated 메서드)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiotService {

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

    // === 🎯 새로운 토이프로젝트용 메서드들 ===

    /**
     * 🎯 간단한 플레이어 정보 조회 (토이프로젝트 MVP)
     */
    public PlayerResponse getSimplePlayer(String gameName, String tagLine, String platform) {
        try {
            log.info("🎯 간단한 플레이어 정보 조회: {}#{} ({})", gameName, tagLine, platform);
            
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            SummonerResponse summoner = getSummonerByPuuid(platform, account.getPuuid());
            List<RankResponse> ranks = getRankInfoSafely(platform, summoner.getId());
            
            String soloRank = ranks.stream()
                    .filter(r -> "RANKED_SOLO_5x5".equals(r.getQueueType()))
                    .findFirst()
                    .map(r -> {
                        if ("UNRANKED".equals(r.getTier())) {
                            return "UNRANKED";
                        }
                        return r.getTier() + " " + r.getRank() + " (" + r.getLeaguePoints() + "LP)";
                    })
                    .orElse("UNRANKED");
            
            return PlayerResponse.createSimple(
                    account.getGameName(),
                    account.getTagLine(),
                    summoner.getName(),
                    summoner.getSummonerLevel(),
                    summoner.getProfileIconId(),
                    soloRank
            );
            
        } catch (Exception e) {
            log.error("💥 간단한 플레이어 정보 조회 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 정보를 가져올 수 없습니다: " + gameName + "#" + tagLine);
        }
    }

    /**
     * 🌟 상세한 플레이어 정보 조회 (전체 정보 포함)
     */
    public PlayerResponse getDetailedPlayer(String gameName, String tagLine, String platform) {
        try {
            log.info("🌟 상세한 플레이어 정보 조회: {}#{} ({})", gameName, tagLine, platform);
            
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            SummonerResponse summoner = getSummonerByPuuid(platform, account.getPuuid());
            
            List<RankResponse> ranks = getRankInfoSafely(platform, summoner.getId());
            List<String> recentMatches = getRecentMatchIdsSafely(account.getPuuid(), 5);
            List<ChampionMasteryResponse> topChampions = getChampionMasterySafely(platform, account.getPuuid(), 3);
            
            return PlayerResponse.createDetailed(
                    account.getGameName(),
                    account.getTagLine(),
                    account.getPuuid(),
                    summoner.getName(),
                    summoner.getSummonerLevel(),
                    summoner.getProfileIconId(),
                    ranks,
                    recentMatches,
                    topChampions
            );
            
        } catch (Exception e) {
            log.error("💥 상세한 플레이어 정보 조회 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 상세 정보를 가져올 수 없습니다: " + gameName + "#" + tagLine);
        }
    }

    // === 🔧 기본 API 메서드들 ===

    public AccountResponse getAccountByRiotId(String gameName, String tagLine) {
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        
        String url = String.format("https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                regionalRoute, encodedGameName, encodedTagLine);
        
        try {
            log.info("🔍 Account API 호출: {}#{}", gameName, tagLine);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다");
            }
            
            return AccountResponse.builder()
                    .puuid(response.get("puuid").toString())
                    .gameName(response.get("gameName").toString())
                    .tagLine(response.get("tagLine").toString())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("❌ Account API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            
            String userMessage = switch (e.getStatusCode().value()) {
                case 404 -> "Riot ID를 찾을 수 없습니다: " + gameName + "#" + tagLine;
                case 403 -> "API 키 권한이 없습니다";
                case 429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해주세요";
                default -> "일시적인 오류가 발생했습니다";
            };
            
            throw new ResponseStatusException(e.getStatusCode(), userMessage);
        }
    }

    public SummonerResponse getSummonerByPuuid(String platform, String puuid) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s",
                platformCode, puuid);
        
        try {
            log.info("👤 Summoner API 호출: {} ({})", puuid.substring(0, 8), platform);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "소환사 정보를 찾을 수 없습니다");
            }
            
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
            log.error("❌ Summoner API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), "소환사 정보를 가져올 수 없습니다");
        }
    }

    public List<String> getRecentMatchIds(String puuid, int count) {
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d",
                regionalRoute, puuid, Math.min(count, 20));
        
        try {
            log.info("🎯 Match IDs API 호출: {} ({}경기)", puuid.substring(0, 8), count);
            List<String> matchIds = riotRestTemplate.getForObject(url, List.class);
            return matchIds != null ? matchIds : List.of();
            
        } catch (HttpClientErrorException e) {
            log.error("❌ Match IDs API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            
            if (e.getStatusCode().value() == 404) {
                return List.of();
            }
            
            throw new ResponseStatusException(e.getStatusCode(), "경기 목록을 가져올 수 없습니다");
        }
    }

    public MatchDetailResponse getMatchDetail(String matchId) {
        String url = String.format("https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                regionalRoute, matchId);
        
        try {
            log.info("📊 Match Detail API 호출: {}", matchId);
            Map<String, Object> response = riotRestTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "경기 정보를 찾을 수 없습니다");
            }
            
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
            log.error("❌ Match Detail API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), 
                    "경기 상세 정보를 가져올 수 없습니다: " + matchId);
        }
    }

    public List<RankResponse> getRankInfo(String platform, String summonerId) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/league/v4/entries/by-summoner/%s",
                platformCode, summonerId);
        
        try {
            log.info("🏆 Rank API 호출: {} ({})", summonerId, platform);
            List<Map<String, Object>> response = riotRestTemplate.getForObject(url, List.class);
            
            if (response == null || response.isEmpty()) {
                log.info("ℹ️ 랭크 정보 없음: {}", summonerId);
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
            log.error("❌ Rank API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            return List.of();
        }
    }

    public List<ChampionMasteryResponse> getChampionMastery(String platform, String puuid, int count) {
        String platformCode = PLATFORM_MAPPING.getOrDefault(platform.toLowerCase(), platform);
        String url = String.format("https://%s.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/%s/top?count=%d",
                platformCode, puuid, Math.min(count, 10));
        
        try {
            log.info("🎖️ Champion Mastery API 호출: {} (Top {})", puuid.substring(0, 8), count);
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
            log.error("❌ Champion Mastery API 호출 실패: {} - {}", e.getStatusCode(), e.getMessage());
            return List.of();
        }
    }

    // === 💡 안전한 조회 메서드들 (부분 실패 허용) ===
    
    private List<RankResponse> getRankInfoSafely(String platform, String summonerId) {
        try {
            return getRankInfo(platform, summonerId);
        } catch (Exception e) {
            log.warn("⚠️ 랭크 정보 조회 실패, 빈 리스트 반환: {}", e.getMessage());
            return List.of();
        }
    }
    
    private List<String> getRecentMatchIdsSafely(String puuid, int count) {
        try {
            return getRecentMatchIds(puuid, count);
        } catch (Exception e) {
            log.warn("⚠️ 최근 경기 조회 실패, 빈 리스트 반환: {}", e.getMessage());
            return List.of();
        }
    }
    
    private List<ChampionMasteryResponse> getChampionMasterySafely(String platform, String puuid, int count) {
        try {
            return getChampionMastery(platform, puuid, count);
        } catch (Exception e) {
            log.warn("⚠️ 챔피언 숙련도 조회 실패, 빈 리스트 반환: {}", e.getMessage());
            return List.of();
        }
    }

    // === 📦 기존 호환성 메서드 (Deprecated) ===

    @Deprecated
    public PlayerSummaryResponse getPlayerSummary(String gameName, String tagLine, String platform) {
        try {
            log.info("📦 기존 호환성 메서드 호출: {}#{} ({})", gameName, tagLine, platform);
            
            AccountResponse account = getAccountByRiotId(gameName, tagLine);
            SummonerResponse summoner = getSummonerByPuuid(platform, account.getPuuid());
            List<RankResponse> ranks = getRankInfo(platform, summoner.getId());
            List<String> recentMatches = getRecentMatchIds(account.getPuuid(), 5);
            List<ChampionMasteryResponse> topChampions = getChampionMastery(platform, account.getPuuid(), 3);
            
            return PlayerSummaryResponse.builder()
                    .account(account)
                    .summoner(summoner)
                    .ranks(ranks)
                    .recentMatchIds(recentMatches)
                    .topChampions(topChampions)
                    .build();
                    
        } catch (Exception e) {
            log.error("💥 플레이어 종합 정보 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "플레이어 정보를 가져오는 중 오류가 발생했습니다");
        }
    }
}