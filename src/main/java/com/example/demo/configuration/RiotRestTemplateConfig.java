package com.example.demo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RiotRestTemplateConfig {

    @Value("${riot.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate riotRestTemplate() {
        RestTemplate rt = new RestTemplate();
        ClientHttpRequestInterceptor auth = (req, body, exec) -> {
            req.getHeaders().add("X-Riot-Token", apiKey);
            return exec.execute(req, body);
        };
        rt.setInterceptors(Collections.singletonList(auth)); // Java 8 호환
        return rt;
    }
}
