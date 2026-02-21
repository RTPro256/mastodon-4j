package org.joinmastodon.torrent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring configuration for the torrent module.
 */
@Configuration
@EnableConfigurationProperties(TorrentProperties.class)
@ComponentScan(basePackages = "org.joinmastodon.torrent")
@EnableJpaRepositories(basePackages = "org.joinmastodon.torrent.repository")
public class TorrentConfiguration {

}
