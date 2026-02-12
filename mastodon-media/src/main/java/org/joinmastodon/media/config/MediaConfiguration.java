package org.joinmastodon.media.config;

import org.joinmastodon.media.scanning.AvScanner;
import org.joinmastodon.media.scanning.NoOpAvScanner;
import org.joinmastodon.media.storage.LocalFileSystemMediaStorage;
import org.joinmastodon.media.storage.MediaStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaConfiguration {

    @Bean
    public MediaStorage mediaStorage(MediaProperties properties) {
        return new LocalFileSystemMediaStorage(properties.getStoragePath(), properties.getBaseUrl());
    }

    @Bean
    public AvScanner avScanner() {
        return new NoOpAvScanner();
    }
}
