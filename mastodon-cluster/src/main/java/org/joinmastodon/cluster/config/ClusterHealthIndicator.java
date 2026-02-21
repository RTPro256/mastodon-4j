package org.joinmastodon.cluster.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * Health indicator for cluster status.
 */
public class ClusterHealthIndicator implements HealthIndicator {

    private ClusterStatus clusterStatus = ClusterStatus.UNKNOWN;
    private int activeNodes = 0;
    private int totalNodes = 0;
    private String coordinatorNode = null;

    @Override
    public Health health() {
        if (clusterStatus == ClusterStatus.HEALTHY) {
            return Health.up()
                    .withDetail("status", clusterStatus)
                    .withDetail("activeNodes", activeNodes)
                    .withDetail("totalNodes", totalNodes)
                    .withDetail("coordinator", coordinatorNode)
                    .build();
        } else if (clusterStatus == ClusterStatus.DEGRADED) {
            return Health.status(new Status("DEGRADED", "Cluster is operating in degraded mode"))
                    .withDetail("activeNodes", activeNodes)
                    .withDetail("totalNodes", totalNodes)
                    .withDetail("coordinator", coordinatorNode)
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", clusterStatus)
                    .withDetail("activeNodes", activeNodes)
                    .withDetail("totalNodes", totalNodes)
                    .build();
        }
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
