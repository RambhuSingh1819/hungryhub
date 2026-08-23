package com.fooddelivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@Slf4j
public class KeepAliveService {

    @Value("${app.render.url:${APP_RENDER_URL:https://rambhu-hungryhub.onrender.com}}")
    private String renderUrl;

    // 600000 milliseconds = 10 minutes
    @Scheduled(fixedRate = 600000)
    public void pingRenderSite() {
        try {
            if (renderUrl == null || renderUrl.isBlank() || renderUrl.contains("localhost")) {
                return;
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject(renderUrl, String.class);
            log.info("Self-ping successful: Kept server awake at {}", renderUrl);
        } catch (Exception e) {
            log.warn("Keep-alive ping failed for {}: {}", renderUrl, e.getMessage());
        }
    }
}
