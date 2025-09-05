package com.example.demo.riot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionMasteryResponse {
    private int championId;
    private int championLevel;
    private int championPoints;
    private long lastPlayTime;
    private int championPointsSinceLastLevel;
    private int championPointsUntilNextLevel;
}
