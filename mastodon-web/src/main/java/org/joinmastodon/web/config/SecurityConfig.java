package org.joinmastodon.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for development.
 * 
 * This configuration disables all security for easy development.
 * 
 * WARNING: This is for DEVELOPMENT ONLY. 
 * For production, use ProductionSecurityConfig with proper authentication.
 * 
 * This config is active for 'dev' and 'default' profiles only.
 * The 'prod' profile uses ProductionSecurityConfig instead.
 */
@Configuration
@EnableWebSecurity
@Profile({"dev", "default"})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection
            .csrf(csrf -> csrf.disable())
            
            // Disable frame options (allows H2 console to work)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )
            
            // Allow all requests without authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
