package com.example.demo.web;

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
}
