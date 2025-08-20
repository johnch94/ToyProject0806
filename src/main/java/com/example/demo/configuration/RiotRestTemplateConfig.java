package com.example.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays; // ← Arrays.asList 사용

@Configuration
public class RiotRestTemplateConfig {

    @Value("${riot.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate riotRestTemplate() {
        RestTemplate rt = new RestTemplate();

        // 1) 인증 헤더: set + trim (공백/따옴표 등 보정)
        ClientHttpRequestInterceptor auth = (req, body, exec) -> {
            req.getHeaders().set("X-Riot-Token", apiKey == null ? "" : apiKey.trim());
            return exec.execute(req, body);
        };

        // 2) 간단 로깅: 외부 호출/응답 코드 확인
        ClientHttpRequestInterceptor logging = (req, body, exec) -> {
            long t0 = System.currentTimeMillis();
            System.out.println("[RIOT OUT] " + req.getMethod() + " " + req.getURI());
            var resp = exec.execute(req, body);
            System.out.println("[RIOT IN ] " + resp.getStatusCode() + " (" + (System.currentTimeMillis() - t0) + "ms)");
            return resp;
        };

        // 3) 인터셉터 등록 (auth → logging 순)
        rt.setInterceptors(Arrays.asList(auth, logging));
        return rt;
    }
}
