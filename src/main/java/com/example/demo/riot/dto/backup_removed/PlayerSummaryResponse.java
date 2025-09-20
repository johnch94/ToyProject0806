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
    // topChampions 제거 - 토이프로젝트에서는 복잡도 감소를 위해 제외
}
