package com.example.demo.riot;

import com.example.demo.riot.RiotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RiotController {

    private final RiotService riotService;

    // 예: /riot/matches?gameName=Faker&tagLine=KR1&count=3
    @GetMapping("/riot/matches")
    public Map<String, Object> getMatches(
            @RequestParam String gameName,
            @RequestParam String tagLine,
            @RequestParam(defaultValue = "3") int count
    ) {
        String puuid = riotService.getPuuidByRiotId(gameName, tagLine);
        List<String> matchIds = riotService.getMatchIds(puuid, 0, count);

        Map<String, Object> result = new HashMap<>();
        result.put("puuid", puuid);
        result.put("matchIds", matchIds);
        if (!matchIds.isEmpty()) {
            result.put("firstMatchDetail", riotService.getMatchDetail(matchIds.get(0)));
        }
        return result;
    }

    @GetMapping("/riot/matches-by-summoner")
    public Map<String, Object> getMatchesBySummoner(
            @RequestParam(defaultValue = "kr") String platform, // KR 서버면 kr
            @RequestParam String summonerName,
            @RequestParam(defaultValue = "3") int count
    ) {
        // 1) 소환사명 → PUUID (플랫폼 라우트 사용)
        String puuid = riotService.getPuuidBySummonerName(platform, summonerName);

        // 2) PUUID → matchIds (리저널 라우트는 기존 properties의 riot.platform-route 사용: KR은 asia)
        List<String> matchIds = riotService.getMatchIds(puuid, 0, count);

        Map<String, Object> result = new HashMap<>();
        result.put("platform", platform);
        result.put("summonerName", summonerName);
        result.put("puuid", puuid);
        result.put("matchIds", matchIds);
        result.put("firstMatchDetail", matchIds.isEmpty() ? null : riotService.getMatchDetail(matchIds.get(0)));
        return result;
    }

    // (선택) 소환사명 → Riot ID(gameName/tagLine) 확인
    @GetMapping("/riot/resolve-riot-id")
    public Map<String, Object> resolveRiotId(
            @RequestParam(defaultValue = "kr") String platform,
            @RequestParam String summonerName
    ) {
        String puuid = riotService.getPuuidBySummonerName(platform, summonerName);
        Map acc = riotService.getAccountByPuuid(puuid);
        return Map.of(
                "puuid", puuid,
                "riotId", Map.of("gameName", acc.get("gameName"), "tagLine", acc.get("tagLine"))
        );
    }
}
