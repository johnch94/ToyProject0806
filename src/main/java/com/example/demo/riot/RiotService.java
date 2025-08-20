package com.example.demo.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

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
        try {
            Map body = rt.getForObject(url, Map.class);
            if (body == null || body.get("puuid") == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Riot ID를 찾지 못했습니다.");
            }
            return body.get("puuid").toString();
        } catch (HttpClientErrorException e) {
            // Riot의 상태코드를 그대로 반환
            throw new ResponseStatusException(e.getStatusCode(),
                    "Riot API 오류: " + e.getStatusText() + " / " + e.getResponseBodyAsString());
        }
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

    public String getPuuidBySummonerName(String platform, String summonerName) {
        String n = URLEncoder.encode(summonerName, StandardCharsets.UTF_8);
        String url = "https://" + platform + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/" + n;
        try {
            Map body = rt.getForObject(url, java.util.Map.class);
            if (body == null || body.get("puuid") == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "소환사를 찾지 못했습니다.");
            }
            return body.get("puuid").toString();
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(),
                    "Riot API 오류: " + e.getStatusText() + " / " + e.getResponseBodyAsString());
        }
    }

    // RiotService.java
    public Map getAccountByPuuid(String puuid) {
        String url = "https://" + route + ".api.riotgames.com/riot/account/v1/accounts/by-puuid/" + puuid;
        return rt.getForObject(url, Map.class); // { puuid, gameName, tagLine }
    }
}

