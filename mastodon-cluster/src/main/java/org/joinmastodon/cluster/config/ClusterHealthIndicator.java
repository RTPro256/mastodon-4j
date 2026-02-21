package org.joinmastodon.cluster.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator for cluster status.
 * Uses Spring Boot 4.0 Actuator endpoint pattern.
 */
@Component
@Endpoint(id = "cluster")
public class ClusterHealthIndicator {

    private ClusterStatus clusterStatus = ClusterStatus.UNKNOWN;
    private int activeNodes = 0;
    private int totalNodes = 0;
    private String coordinatorNode = null;

    @ReadOperation
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", clusterStatus.name());
        health.put("activeNodes", activeNodes);
        health.put("totalNodes", totalNodes);
        health.put("coordinator", coordinatorNode);
        health.put("healthy", clusterStatus == ClusterStatus.HEALTHY);
        return health;
    }

    public void updateStatus(ClusterStatus status, int activeNodes, int totalNodes, String coordinatorNode) {
        this.clusterStatus = status;
        this.activeNodes = activeNodes;
        this.totalNodes = totalNodes;
        this.coordinatorNode = coordinatorNode;
    }

    /**
     * Cluster status enumeration.
     */
    public enum ClusterStatus {
        /**
         * Cluster is healthy and fully operational.
         */
        HEALTHY,

        /**
         * Cluster is operational but some nodes are unavailable.
         */
        DEGRADED,

        /**
         * Cluster is not operational.
         */
        DOWN,

        /**
         * Cluster status is unknown.
         */
        UNKNOWN
    }
}
