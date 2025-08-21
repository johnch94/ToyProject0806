package com.example.demo.riot.dto;

import lombok.*;
import java.util.List;

/**
 * 🎮 플레이어 통합 정보 DTO (토이프로젝트용)
 * 
 * 기존 PlayerSummaryResponse + SimplePlayerResponse 통합
 * 
 * 설계 원칙:
 * - 필수 정보만 포함 (MVP 중심)
 * - null 처리 가능한 옵션 필드
 * - 프론트엔드 사용 편의성 고려
 * 
 * 실무 적용 시:
 * - @JsonInclude(JsonInclude.Include.NON_NULL) 추가
 * - 버전 관리를 위한 @JsonIgnoreProperties 추가
 * - API 응답 시간 단축을 위한 lazy loading 고려
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponse {
    
    // === 기본 계정 정보 (필수) ===
    private String gameName;        // Riot ID 게임명
    private String tagLine;         // Riot ID 태그
    private String puuid;           // 고유 식별자
    
    // === 소환사 정보 (필수) ===
    private String summonerName;    // 게임 내 소환사명
    private int summonerLevel;      // 소환사 레벨
    private int profileIconId;      // 프로필 아이콘 ID
    
    // === 랭크 정보 (선택적) ===
    private String soloRank;        // 솔로랭크 요약 (예: "GOLD II")
    private String flexRank;        // 자유랭크 요약 (예: "SILVER I")
    private List<RankResponse> detailedRanks; // 상세 랭크 정보 (필요시만)
    
    // === 게임 기록 (선택적) ===
    private List<String> recentMatchIds;      // 최근 경기 ID 목록 (최대 5개)
    private List<ChampionMasteryResponse> topChampions; // 주력 챔피언 (최대 3개)
    
    /**
     * 간단한 랭크 정보만 필요한 경우를 위한 팩토리 메서드
     */
    public static PlayerResponse createSimple(String gameName, String tagLine, String summonerName, 
                                            int summonerLevel, int profileIconId, String soloRank) {
        return PlayerResponse.builder()
                .gameName(gameName)
                .tagLine(tagLine)
                .summonerName(summonerName)
                .summonerLevel(summonerLevel)
                .profileIconId(profileIconId)
                .soloRank(soloRank)
                .build();
    }
    
    /**
     * 전체 정보가 포함된 경우를 위한 팩토리 메서드
     */
    public static PlayerResponse createDetailed(String gameName, String tagLine, String puuid,
                                              String summonerName, int summonerLevel, int profileIconId,
                                              List<RankResponse> ranks, List<String> matchIds, 
                                              List<ChampionMasteryResponse> champions) {
        // 랭크 정보에서 솔로/자유랭크 추출
        String soloRank = extractRankString(ranks, "RANKED_SOLO_5x5");
        String flexRank = extractRankString(ranks, "RANKED_FLEX_SR");
        
        return PlayerResponse.builder()
                .gameName(gameName)
                .tagLine(tagLine)
                .puuid(puuid)
                .summonerName(summonerName)
                .summonerLevel(summonerLevel)
                .profileIconId(profileIconId)
                .soloRank(soloRank)
                .flexRank(flexRank)
                .detailedRanks(ranks)
                .recentMatchIds(matchIds)
                .topChampions(champions)
                .build();
    }
    
    /**
     * 랭크 목록에서 특정 큐 타입의 랭크 문자열 추출
     */
    private static String extractRankString(List<RankResponse> ranks, String queueType) {
        if (ranks == null || ranks.isEmpty()) {
            return "UNRANKED";
        }
        
        return ranks.stream()
                .filter(r -> queueType.equals(r.getQueueType()))
                .findFirst()
                .map(r -> {
                    if ("UNRANKED".equals(r.getTier())) {
                        return "UNRANKED";
                    }
                    return r.getTier() + " " + r.getRank() + " (" + r.getLeaguePoints() + "LP)";
                })
                .orElse("UNRANKED");
    }
    
    /**
     * 프론트엔드에서 사용할 디스플레이용 Riot ID
     */
    public String getDisplayRiotId() {
        return gameName + "#" + tagLine;
    }
    
    /**
     * 주력 챔피언이 있는지 확인
     */
    public boolean hasTopChampions() {
        return topChampions != null && !topChampions.isEmpty();
    }
    
    /**
     * 최근 경기가 있는지 확인
     */
    public boolean hasRecentMatches() {
        return recentMatchIds != null && !recentMatchIds.isEmpty();
    }
}