package org.joinmastodon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for Mastodon Java.
 * 
 * This application serves as the entry point for the Mastodon Java migration project,
 * which aims to reimplement the Mastodon federated social network using OpenJDK 25
 * and Spring Boot 4.0.
 * 
 * @author Mastodon Java Team
 * @version 0.1.0-SNAPSHOT
 */
@SpringBootApplication(scanBasePackages = "org.joinmastodon")
public class MastodonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MastodonApplication.class, args);
    }
}
