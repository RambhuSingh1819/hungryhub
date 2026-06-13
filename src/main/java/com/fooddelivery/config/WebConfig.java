package com.fooddelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String userDir = System.getProperty("user.dir");
        String absolutePath = "file:" + userDir + "/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }
}
