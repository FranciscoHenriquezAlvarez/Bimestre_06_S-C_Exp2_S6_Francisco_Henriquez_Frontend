package com.duoc.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Cliente HTTP del lado del servidor para consumir el backend sin exponer llamadas del navegador a otro puerto.
        return new RestTemplate();
    }
}
