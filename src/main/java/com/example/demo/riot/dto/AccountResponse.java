package com.example.demo.riot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private String puuid;
    private String gameName;
    private String tagLine;
}
