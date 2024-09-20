package com.swd392.ticket_resell_be.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "payos")
public class PayOSConfiguration {

    // Getters and Setters
    private String clientId;
    private String apiKey;
    private String checksumKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
