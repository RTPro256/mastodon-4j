package org.joinmastodon.cluster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Node-specific properties for cluster configuration.
 */
@ConfigurationProperties(prefix = "mastodon.cluster.node")
public class NodeProperties {

    /**
     * Unique node identifier.
     */
    private String id;

    /**
     * Human-readable name for this node.
     */
    private String name;

    /**
     * Host address for inter-node communication.
     */
    private String host;

    /**
     * Port for cluster communication.
     */
    private int port = 7946;

    /**
     * API endpoint for this node.
     */
    private String apiEndpoint;

    /**
     * Capabilities of this node.
     */
    private List<NodeCapability> capabilities = new ArrayList<>();

    /**
     * Maximum concurrent tasks this node can handle.
     */
    private int maxTasks = 100;

    /**
     * Node weight for load balancing (higher = more capacity).
     */
    private int weight = 1;

    /**
     * Region/zone for geographic distribution.
     */
    private String region;

    /**
     * Zone within region.
     */
    private String zone;

    /**
     * Tags for custom routing and scheduling.
     */
    private List<String> tags = new ArrayList<>();

    /**
     * Health check configuration.
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public List<NodeCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<NodeCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public HealthCheckConfig getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckConfig healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     * Node capabilities.
     */
    public enum NodeCapability {
        /**
         * Handle API requests.
         */
        API,

        /**
         * Handle streaming connections.
         */
        STREAMING,

        /**
         * Process media uploads and transcoding.
         */
        MEDIA,

        /**
         * Execute background jobs.
         */
        JOBS,

        /**
         * Handle federation traffic.
         */
        FEDERATION,

        /**
         * Act as coordinator.
         */
        COORDINATOR
    }

    /**
     * Health check configuration.
     */
    public static class HealthCheckConfig {
        private boolean enabled = true;
        private Duration interval = Duration.ofSeconds(10);
        private Duration timeout = Duration.ofSeconds(5);
        private int failureThreshold = 3;
        private int successThreshold = 2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public int getSuccessThreshold() {
            return successThreshold;
        }

        public void setSuccessThreshold(int successThreshold) {
            this.successThreshold = successThreshold;
        }
    }
}
