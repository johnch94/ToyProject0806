package com.example.demo.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class RiotService {

    private final RestTemplate rt;

    @Value("${riot.platform-route}")
    private String route; // asia | americas | europe

    public RiotService(RestTemplate rt) {
        this.rt = rt;
    }

    public String getPuuidByRiotId(String gameName, String tagLine) {
        String g = URLEncoder.encode(gameName, StandardCharsets.UTF_8);
        String t = URLEncoder.encode(tagLine, StandardCharsets.UTF_8);
        String url = "https://" + route + ".api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + g + "/" + t;
        Map body = rt.getForObject(url, Map.class);
        if (body == null || body.get("puuid") == null) throw new IllegalStateException("PUUID 조회 실패");
        return body.get("puuid").toString();
    }

    public List<String> getMatchIds(String puuid, int start, int count) {
        String url = "https://" + route + ".api.riotgames.com/lol/match/v5/matches/by-puuid/" + puuid
                + "/ids?start=" + start + "&count=" + count;
        return rt.getForObject(url, List.class);
    }

    public Map getMatchDetail(String matchId) {
        String url = "https://" + route + ".api.riotgames.com/lol/match/v5/matches/" + matchId;
        return rt.getForObject(url, Map.class);
    }
}
