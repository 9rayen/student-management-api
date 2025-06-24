package com.example.demo.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Configuration for JWT service HTTP client communication
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Dedicated RestTemplate for JWT service communication with custom timeouts
     */
    @Bean("jwtServiceRestTemplate")
    public RestTemplate jwtServiceRestTemplate(JwtProperties jwtProperties) {
        JwtProperties.CentralizedService centralizedConfig = jwtProperties.getCentralizedService();
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(centralizedConfig.getConnectionTimeout());
        factory.setReadTimeout(centralizedConfig.getReadTimeout());
        
        return new RestTemplate(factory);
    }
}
