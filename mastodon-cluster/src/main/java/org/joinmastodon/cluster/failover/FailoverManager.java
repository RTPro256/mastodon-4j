package org.joinmastodon.cluster.failover;

import org.joinmastodon.cluster.config.ClusterProperties;
import org.joinmastodon.cluster.config.NodeProperties;
import org.joinmastodon.cluster.discovery.ClusterNode;
import org.joinmastodon.cluster.discovery.NodeRegistry;
import org.joinmastodon.cluster.distribution.TaskQueue;
import org.joinmastodon.cluster.sync.ClusterEvent;
import org.joinmastodon.cluster.sync.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages failover and recovery in the cluster.
 */
@Component
public class FailoverManager {

    private static final Logger log = LoggerFactory.getLogger(FailoverManager.class);

    private final ClusterProperties clusterProperties;
    private final NodeProperties nodeProperties;
    private final NodeRegistry nodeRegistry;
    private final TaskQueue taskQueue;
    private final EventBus eventBus;

    private final AtomicBoolean inFailover = new AtomicBoolean(false);
    private volatile Instant lastFailoverTime;

    public FailoverManager(ClusterProperties clusterProperties, NodeProperties nodeProperties,
                          NodeRegistry nodeRegistry, TaskQueue taskQueue, EventBus eventBus) {
        this.clusterProperties = clusterProperties;
        this.nodeProperties = nodeProperties;
        this.nodeRegistry = nodeRegistry;
        this.taskQueue = taskQueue;
        this.eventBus = eventBus;

        // Subscribe to node failure events
        eventBus.subscribe(ClusterEvent.Types.NODE_FAILED, this::handleNodeFailure);
        eventBus.subscribe(ClusterEvent.Types.NODE_RECOVERED, this::handleNodeRecovery);
    }

    /**
     * Handle a node failure.
     */
    public void handleNodeFailure(ClusterEvent event) {
        String failedNodeId = (String) event.getData().get("nodeId");
        log.warn("Handling failure of node: {}", failedNodeId);

        if (!inFailover.compareAndSet(false, true)) {
            log.info("Failover already in progress, queuing node failure: {}", failedNodeId);
            return;
        }

        try {
            lastFailoverTime = Instant.now();
            eventBus.publish(new ClusterEvent(
                    ClusterEvent.Types.FAILOVER_STARTED,
                    nodeProperties.getId(),
                    event.getData()));

            // Mark node as unavailable
            nodeRegistry.markUnavailable(failedNodeId);

            // Reassign tasks from failed node
            int reassignedTasks = taskQueue.reassignFromNode(failedNodeId);
            log.info("Reassigned {} tasks from failed node {}", reassignedTasks, failedNodeId);

            // Check if coordinator failed
            Optional<ClusterNode> coordinator = nodeRegistry.getCoordinator();
            if (coordinator.isPresent() && coordinator.get().getId().equals(failedNodeId)) {
                electNewCoordinator();
            }

            eventBus.publish(new ClusterEvent(
                    ClusterEvent.Types.FAILOVER_COMPLETED,
                    nodeProperties.getId(),
                    event.getData()));

        } finally {
            inFailover.set(false);
        }
    }

    /**
     * Handle a node recovery.
     */
    public void handleNodeRecovery(ClusterEvent event) {
        String recoveredNodeId = event.getSourceNodeId();
        log.info("Node recovered: {}", recoveredNodeId);

        nodeRegistry.getNode(recoveredNodeId).ifPresent(node -> {
            node.setState(ClusterNode.NodeState.ACTIVE);
            log.info("Node {} is now active", recoveredNodeId);
        });
    }

    /**
     * Elect a new coordinator.
     */
    private void electNewCoordinator() {
        log.info("Starting coordinator election");

        // Simple election: choose the active node with the lowest ID
        // In a real implementation, this would use a more sophisticated algorithm (Raft, Bully, etc.)
        List<ClusterNode> activeNodes = nodeRegistry.getActiveNodes();
        if (activeNodes.isEmpty()) {
            log.error("No active nodes available for coordinator election");
            return;
        }

        // Filter nodes with COORDINATOR capability
        List<ClusterNode> eligibleNodes = activeNodes.stream()
                .filter(n -> n.hasCapability(NodeProperties.NodeCapability.COORDINATOR))
                .toList();

        if (eligibleNodes.isEmpty()) {
            // Fall back to any active node
            eligibleNodes = activeNodes;
        }

        // Select the node with the lowest ID (deterministic)
        ClusterNode newCoordinator = eligibleNodes.stream()
                .min((n1, n2) -> n1.getId().compareTo(n2.getId()))
                .orElseThrow();

        nodeRegistry.setCoordinator(newCoordinator.getId());
        log.info("Elected new coordinator: {}", newCoordinator.getId());

        eventBus.publish(new ClusterEvent(
                ClusterEvent.Types.COORDINATOR_CHANGED,
                nodeProperties.getId(),
                java.util.Map.of(
                        "oldCoordinator", "unknown",
                        "newCoordinator", newCoordinator.getId())));
    }

    /**
     * Check if this node should become coordinator.
     */
    public boolean shouldBecomeCoordinator() {
        if (!clusterProperties.isEnabled()) {
            return false;
        }

        ClusterProperties.NodeRole role = clusterProperties.getRole();
        return role == ClusterProperties.NodeRole.COORDINATOR || 
               role == ClusterProperties.NodeRole.HYBRID;
    }

    /**
     * Scheduled health check for failover detection.
     */
    @Scheduled(fixedDelayString = "${mastodon.cluster.failover.heartbeat-interval:5000}")
    public void checkClusterHealth() {
        if (!clusterProperties.isEnabled() || !clusterProperties.getFailover().isEnabled()) {
            return;
        }

        // Only coordinator performs health checks
        if (!nodeRegistry.isCoordinator()) {
            return;
        }

        NodeRegistry.ClusterStats stats = nodeRegistry.getStats();
        
        // Check for unavailable nodes
        if (stats.getUnavailableNodes() > 0) {
            log.warn("Cluster has {} unavailable nodes out of {}", 
                    stats.getUnavailableNodes(), stats.getTotalNodes());
        }

        // Check cluster capacity
        double utilization = stats.getUtilization();
        if (utilization > 0.9) {
            log.warn("Cluster utilization is high: {}%", utilization * 100);
        }
    }

    /**
     * Get failover status.
     */
    public FailoverStatus getStatus() {
        return new FailoverStatus(
                inFailover.get(),
                lastFailoverTime,
                nodeRegistry.isCoordinator(),
                nodeRegistry.getStats().getActiveNodes(),
                nodeRegistry.getStats().getUnavailableNodes());
    }

    /**
     * Failover status.
     */
    public static class FailoverStatus {
        private final boolean inFailover;
        private final Instant lastFailoverTime;
        private final boolean isCoordinator;
        private final int activeNodes;
        private final int unavailableNodes;

        public FailoverStatus(boolean inFailover, Instant lastFailoverTime,
                             boolean isCoordinator, int activeNodes, int unavailableNodes) {
            this.inFailover = inFailover;
            this.lastFailoverTime = lastFailoverTime;
            this.isCoordinator = isCoordinator;
            this.activeNodes = activeNodes;
            this.unavailableNodes = unavailableNodes;
        }

        public boolean isInFailover() {
            return inFailover;
        }

        public Instant getLastFailoverTime() {
            return lastFailoverTime;
        }

        public boolean isCoordinator() {
            return isCoordinator;
        }

        public int getActiveNodes() {
            return activeNodes;
        }

        public int getUnavailableNodes() {
            return unavailableNodes;
        }

        public boolean isHealthy() {
            return !inFailover && unavailableNodes == 0;
        }
    }
}
