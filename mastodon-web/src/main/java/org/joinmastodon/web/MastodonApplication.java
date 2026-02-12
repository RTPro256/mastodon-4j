package org.joinmastodon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EntityScan(basePackages = {
        "org.joinmastodon.core.entity",
        "org.joinmastodon.jobs"
})
@EnableJpaRepositories(basePackages = {
        "org.joinmastodon.core.repository",
        "org.joinmastodon.jobs"
})
@EnableScheduling
public class MastodonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MastodonApplication.class, args);
    }
}
