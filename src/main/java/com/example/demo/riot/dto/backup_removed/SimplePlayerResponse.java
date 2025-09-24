package com.example.demo.riot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePlayerResponse {
    private String gameName;
    private String tagLine;
    private String summonerName;
    private int summonerLevel;
    private int profileIconId;
    private String soloRank;
}
