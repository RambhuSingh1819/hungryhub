package com.fooddelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
public class SecurityConfig {

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}


    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    //     http
    //         .csrf(AbstractHttpConfigurer::disable)

    //         .authorizeHttpRequests(auth -> auth
    //             // EVERYTHING allowed — controller controls access
    //             .anyRequest().permitAll()
    //         )

    //         .formLogin(AbstractHttpConfigurer::disable)
    //         .httpBasic(AbstractHttpConfigurer::disable)
    //         .logout(AbstractHttpConfigurer::disable);

    //     http.headers(headers -> headers.frameOptions().disable());

    //     return http.build();
    // }
}
