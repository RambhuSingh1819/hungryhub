package com.fooddelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class FoodDeliveryApplication {

    public static void main(String[] args) {
        System.out.println("USERNAME = " + System.getenv("username"));
        System.out.println("PASSWORD = " + System.getenv("password"));
        SpringApplication.run(FoodDeliveryApplication.class, args);
    }
}
