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

        // 로깅 인터셉터: 외부 호출/응답 코드 확인
        ClientHttpRequestInterceptor logging = (req, body, exec) -> {
            long t0 = System.currentTimeMillis();
            System.out.println("[RIOT OUT] " + req.getMethod() + " " + req.getURI());
            var resp = exec.execute(req, body);
            System.out.println("[RIOT IN ] " + resp.getStatusCode() + " (" + (System.currentTimeMillis() - t0) + "ms)");
            return resp;
        };

        // 로깅 인터셉터만 등록 (API 키는 Query Parameter로 직접 전달)
        rt.setInterceptors(Arrays.asList(logging));
        return rt;
    }
    
    /**
     * API 키를 Query Parameter로 추가하는 헬퍼 메서드
     */
    public String addApiKeyToUrl(String baseUrl) {
        String cleanApiKey = apiKey == null ? "" : apiKey.trim();
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "api_key=" + cleanApiKey;
    }
}
