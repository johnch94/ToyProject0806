package com.example.demo.riot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankResponse {
    private String queueType;      // RANKED_SOLO_5x5, RANKED_FLEX_SR
    private String tier;           // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER
    private String rank;           // I, II, III, IV
    private int leaguePoints;
    private int wins;
    private int losses;
    
    public double getWinRate() {
        int total = wins + losses;
        return total > 0 ? (double) wins / total * 100 : 0.0;
    }
}
