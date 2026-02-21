package org.joinmastodon.cluster.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for cluster module.
 */
@Configuration
@EnableConfigurationProperties({ClusterProperties.class, NodeProperties.class})
@EnableScheduling
public class ClusterConfiguration {

    @Bean
    public ClusterHealthIndicator clusterHealthIndicator() {
        return new ClusterHealthIndicator();
    }
}
