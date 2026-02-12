package org.joinmastodon.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.joinmastodon")
public class MastodonStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MastodonStreamingApplication.class, args);
    }
}
