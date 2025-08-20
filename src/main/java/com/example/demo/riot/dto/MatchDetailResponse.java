package com.example.demo.riot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailResponse {
    private String matchId;
    private String gameMode;
    private long gameDuration;
    private long gameCreation;
    private int participantCount;
    private int queueId;
    private int mapId;
}
