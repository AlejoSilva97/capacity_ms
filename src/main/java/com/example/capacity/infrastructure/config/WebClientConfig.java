package com.example.capacity.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${adapters.technology-ms.url}")
    private String technologyServiceUrl;

    @Bean
    public WebClient technologyWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(technologyServiceUrl)
                .build();
    }
}