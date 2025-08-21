package com.example.demo.riot;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.riot.dto.PlayerResponse;
import com.example.demo.riot.dto.MatchDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎮 Riot API 컨트롤러 (토이프로젝트용 MVP)
 * 
 * 설계 원칙:
 * - 핵심 3개 API만 제공 (복잡성 제거)
 * - 프론트엔드 사용 편의성 중심
 * - 에러 처리 간소화 (학습용)
 * 
 * MVP 기능:
 * 1. 플레이어 기본 정보 조회 (가장 많이 사용)
 * 2. 최근 경기 목록 (경기 분석용)
 * 3. 경기 상세 정보 (경기 분석용)
 * 
 * 실무에서는:
 * - Rate Limiting 적용
 * - 캐싱 전략 구현
 * - Circuit Breaker 패턴 적용
 * - 상세한 모니터링 및 로깅
 */
@RestController
@RequestMapping("/api/riot")
@RequiredArgsConstructor
@Slf4j
public class RiotController {

    private final RiotService riotService;

    /**
     * 🎯 플레이어 정보 조회 (MVP 핵심 API)
     * 
     * 왜 이 방식인가?
     * - 한 번의 호출로 필요한 기본 정보 모두 제공
     * - 프론트엔드에서 추가 API 호출 불필요
     * - 응답 속도와 정보량의 균형점
     * 
     * 매개변수:
     * - detailed: true면 전체 정보, false면 간단 정보
     */
    @GetMapping("/player")
    public ApiResponse<PlayerResponse> getPlayer(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "kr") String platform,
            @RequestParam(defaultValue = "false") boolean detailed) {
        
        log.info("🎯 플레이어 정보 조회: {}#{} (상세: {})", gameName, tagLine, detailed);
        
        PlayerResponse player = detailed 
            ? riotService.getDetailedPlayer(gameName, tagLine, platform)
            : riotService.getSimplePlayer(gameName, tagLine, platform);
        
        return ApiResponse.<PlayerResponse>builder()
                .success(true)
                .message("플레이어 정보 조회 성공")
                .data(player)
                .build();
    }

    /**
     * 📋 최근 경기 목록 조회
     * 
     * 학습 포인트:
     * - PUUID를 통한 경기 검색
     * - 페이징 개념 (count 파라미터)
     * - 외부 API 응답 처리
     */
    @GetMapping("/matches/recent")
    public ApiResponse<List<String>> getRecentMatches(
            @RequestParam String puuid,
            @RequestParam(defaultValue = "5") int count) {
        
        log.info("📋 최근 경기 목록 조회: {} ({}경기)", puuid.substring(0, 8), count);
        
        List<String> matchIds = riotService.getRecentMatchIds(puuid, count);
        
        return ApiResponse.<List<String>>builder()
                .success(true)
                .message(String.format("최근 %d경기 조회 성공", matchIds.size()))
                .data(matchIds)
                .build();
    }

    /**
     * 🔍 경기 상세 정보 조회
     * 
     * 추후 확장 가능:
     * - 참가자별 상세 통계
     * - 아이템 빌드 정보
     * - 팀 조합 분석
     */
    @GetMapping("/match/{matchId}")
    public ApiResponse<MatchDetailResponse> getMatchDetail(@PathVariable String matchId) {
        
        log.info("🔍 경기 상세 정보 조회: {}", matchId);
        
        MatchDetailResponse matchDetail = riotService.getMatchDetail(matchId);
        
        return ApiResponse.<MatchDetailResponse>builder()
                .success(true)
                .message("경기 상세 정보 조회 성공")
                .data(matchDetail)
                .build();
    }

    // === 🔧 개발/테스트용 헬퍼 API ===

    /**
     * 🛠️ API 상태 확인 (개발용)
     * 
     * 용도:
     * - Riot API 연결 상태 확인
     * - API 키 유효성 검증
     * - 개발 환경 디버깅
     */
    @GetMapping("/health")
    public ApiResponse<String> checkHealth() {
        log.info("🛠️ Riot API 상태 확인");
        
        try {
            // 간단한 API 호출로 상태 확인 (예: 고정된 유명 플레이어)
            // 간단한 테스트로 API 키 유효성 확인
            riotService.getAccountByRiotId("Faker", "KR1");
            
            String testMessage = "API 키 및 연결 상태 정상";
            
            return ApiResponse.<String>builder()
                    .success(true)
                    .message("Riot API 정상 작동")
                    .data(testMessage)
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Riot API 상태 확인 실패: {}", e.getMessage());
            
            return ApiResponse.<String>builder()
                    .success(false)
                    .message("Riot API 연결 실패")
                    .data("오류 내용: " + e.getMessage())
                    .build();
        }
    }
}