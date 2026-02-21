package org.joinmastodon.cluster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Cluster configuration properties.
 */
@ConfigurationProperties(prefix = "mastodon.cluster")
public class ClusterProperties {

    /**
     * Enable cluster mode.
     */
    private boolean enabled = false;

    /**
     * Unique identifier for this node.
     */
    private String nodeId;

    /**
     * Role of this node in the cluster.
     */
    private NodeRole role = NodeRole.HYBRID;

    /**
     * Coordinator configuration.
     */
    private CoordinatorConfig coordinator = new CoordinatorConfig();

    /**
     * Discovery configuration.
     */
    private DiscoveryConfig discovery = new DiscoveryConfig();

    /**
     * Replication configuration.
     */
    private ReplicationConfig replication = new ReplicationConfig();

    /**
     * Failover configuration.
     */
    private FailoverConfig failover = new FailoverConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public NodeRole getRole() {
        return role;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public CoordinatorConfig getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(CoordinatorConfig coordinator) {
        this.coordinator = coordinator;
    }

    public DiscoveryConfig getDiscovery() {
        return discovery;
    }

    public void setDiscovery(DiscoveryConfig discovery) {
        this.discovery = discovery;
    }

    public ReplicationConfig getReplication() {
        return replication;
    }

    public void setReplication(ReplicationConfig replication) {
        this.replication = replication;
    }

    public FailoverConfig getFailover() {
        return failover;
    }

    public void setFailover(FailoverConfig failover) {
        this.failover = failover;
    }

    /**
     * Node roles in the cluster.
     */
    public enum NodeRole {
        /**
         * Manages cluster state, distributes work, handles failover.
         */
        COORDINATOR,

        /**
         * Processes assigned tasks (API, streaming, media, jobs).
         */
        WORKER,

        /**
         * Both coordinator and worker capabilities.
         */
        HYBRID
    }

    /**
     * Coordinator configuration.
     */
    public static class CoordinatorConfig {
        private String host;
        private int port = 7946;

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
    }

    /**
     * Discovery configuration.
     */
    public static class DiscoveryConfig {
        private DiscoveryType type = DiscoveryType.STATIC;
        private List<String> endpoints = new ArrayList<>();

        public DiscoveryType getType() {
            return type;
        }

        public void setType(DiscoveryType type) {
            this.type = type;
        }

        public List<String> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<String> endpoints) {
            this.endpoints = endpoints;
        }
    }

    /**
     * Discovery types.
     */
    public enum DiscoveryType {
        CONSUL,
        ETCD,
        STATIC,
        MULTICAST
    }

    /**
     * Replication configuration.
     */
    public static class ReplicationConfig {
        private int factor = 2;
        private Duration syncInterval = Duration.ofSeconds(5);

        public int getFactor() {
            return factor;
        }

        public void setFactor(int factor) {
            this.factor = factor;
        }

        public Duration getSyncInterval() {
            return syncInterval;
        }

        public void setSyncInterval(Duration syncInterval) {
            this.syncInterval = syncInterval;
        }
    }

    /**
     * Failover configuration.
     */
    public static class FailoverConfig {
        private boolean enabled = true;
        private Duration heartbeatInterval = Duration.ofSeconds(5);
        private int maxMissedHeartbeats = 3;
        private Duration recoveryTimeout = Duration.ofMinutes(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public void setHeartbeatInterval(Duration heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }

        public int getMaxMissedHeartbeats() {
            return maxMissedHeartbeats;
        }

        public void setMaxMissedHeartbeats(int maxMissedHeartbeats) {
            this.maxMissedHeartbeats = maxMissedHeartbeats;
        }

        public Duration getRecoveryTimeout() {
            return recoveryTimeout;
        }

        public void setRecoveryTimeout(Duration recoveryTimeout) {
            this.recoveryTimeout = recoveryTimeout;
        }
    }
}
