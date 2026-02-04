package com.fooddelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String absolutePath = "file:/Users/rambhusingh/Desktop/Spring-Boot(Durgesh sir)/FirstSTSProject/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }
}
