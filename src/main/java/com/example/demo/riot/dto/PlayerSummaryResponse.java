package com.example.demo.riot.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerSummaryResponse {
    private AccountResponse account;
    private SummonerResponse summoner;
    private List<RankResponse> ranks;
    private List<String> recentMatchIds;
    private List<ChampionMasteryResponse> topChampions;
}
