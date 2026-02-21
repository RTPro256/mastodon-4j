package org.joinmastodon.cluster.discovery;

import org.joinmastodon.cluster.config.ClusterProperties;
import org.joinmastodon.cluster.config.NodeProperties;
import org.joinmastodon.cluster.config.NodeProperties.NodeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for cluster nodes.
 * Maintains the list of known nodes and their states.
 */
@Component
public class NodeRegistry {

    private static final Logger log = LoggerFactory.getLogger(NodeRegistry.class);

    private final ClusterProperties clusterProperties;
    private final NodeProperties nodeProperties;
    private final Map<String, ClusterNode> nodes = new ConcurrentHashMap<>();
    private volatile String coordinatorId;

    public NodeRegistry(ClusterProperties clusterProperties, NodeProperties nodeProperties) {
        this.clusterProperties = clusterProperties;
        this.nodeProperties = nodeProperties;
    }

    /**
     * Register a new node in the cluster.
     */
    public void register(ClusterNode node) {
        log.info("Registering node: {} ({})", node.getName(), node.getId());
        node.setLastHeartbeat(Instant.now());
        nodes.put(node.getId(), node);
    }

    /**
     * Unregister a node from the cluster.
     */
    public void unregister(String nodeId) {
        log.info("Unregistering node: {}", nodeId);
        ClusterNode removed = nodes.remove(nodeId);
        if (removed != null) {
            removed.setState(ClusterNode.NodeState.LEFT);
        }
    }

    /**
     * Get a node by ID.
     */
    public Optional<ClusterNode> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    /**
     * Get all registered nodes.
     */
    public Collection<ClusterNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Get all active nodes.
     */
    public List<ClusterNode> getActiveNodes() {
        return nodes.values().stream()
                .filter(node -> node.getState() == ClusterNode.NodeState.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Get nodes with a specific capability.
     */
    public List<ClusterNode> getNodesWithCapability(NodeCapability capability) {
        return nodes.values().stream()
                .filter(node -> node.getState() == ClusterNode.NodeState.ACTIVE)
                .filter(node -> node.hasCapability(capability))
                .collect(Collectors.toList());
    }

    /**
     * Update node heartbeat.
     */
    public void updateHeartbeat(String nodeId, int currentTasks, double loadAverage) {
        ClusterNode node = nodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(Instant.now());
            node.setCurrentTasks(currentTasks);
            node.setLoadAverage(loadAverage);
            if (node.getState() == ClusterNode.NodeState.UNAVAILABLE) {
                node.setState(ClusterNode.NodeState.ACTIVE);
                log.info("Node {} recovered and is now active", nodeId);
            }
        }
    }

    /**
     * Mark a node as unavailable.
     */
    public void markUnavailable(String nodeId) {
        ClusterNode node = nodes.get(nodeId);
        if (node != null && node.getState() != ClusterNode.NodeState.UNAVAILABLE) {
            log.warn("Marking node {} as unavailable", nodeId);
            node.setState(ClusterNode.NodeState.UNAVAILABLE);
        }
    }

    /**
     * Set the coordinator node.
     */
    public void setCoordinator(String nodeId) {
        this.coordinatorId = nodeId;
        log.info("Coordinator set to: {}", nodeId);
    }

    /**
     * Get the coordinator node.
     */
    public Optional<ClusterNode> getCoordinator() {
        if (coordinatorId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodes.get(coordinatorId));
    }

    /**
     * Check if this node is the coordinator.
     */
    public boolean isCoordinator() {
        return nodeProperties.getId().equals(coordinatorId);
    }

    /**
     * Get the current node (this instance).
     */
    public Optional<ClusterNode> getCurrentNode() {
        return getNode(nodeProperties.getId());
    }

    /**
     * Get cluster statistics.
     */
    public ClusterStats getStats() {
        int total = nodes.size();
        int active = (int) nodes.values().stream()
                .filter(n -> n.getState() == ClusterNode.NodeState.ACTIVE)
                .count();
        int unavailable = (int) nodes.values().stream()
                .filter(n -> n.getState() == ClusterNode.NodeState.UNAVAILABLE)
                .count();
        int totalCapacity = nodes.values().stream()
                .filter(n -> n.getState() == ClusterNode.NodeState.ACTIVE)
                .mapToInt(ClusterNode::getMaxTasks)
                .sum();
        int currentLoad = nodes.values().stream()
                .filter(n -> n.getState() == ClusterNode.NodeState.ACTIVE)
                .mapToInt(ClusterNode::getCurrentTasks)
                .sum();

        return new ClusterStats(total, active, unavailable, totalCapacity, currentLoad, coordinatorId);
    }

    /**
     * Cluster statistics.
     */
    public static class ClusterStats {
        private final int totalNodes;
        private final int activeNodes;
        private final int unavailableNodes;
        private final int totalCapacity;
        private final int currentLoad;
        private final String coordinatorId;

        public ClusterStats(int totalNodes, int activeNodes, int unavailableNodes,
                           int totalCapacity, int currentLoad, String coordinatorId) {
            this.totalNodes = totalNodes;
            this.activeNodes = activeNodes;
            this.unavailableNodes = unavailableNodes;
            this.totalCapacity = totalCapacity;
            this.currentLoad = currentLoad;
            this.coordinatorId = coordinatorId;
        }

        public int getTotalNodes() {
            return totalNodes;
        }

        public int getActiveNodes() {
            return activeNodes;
        }

        public int getUnavailableNodes() {
            return unavailableNodes;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public int getCurrentLoad() {
            return currentLoad;
        }

        public String getCoordinatorId() {
            return coordinatorId;
        }

        public double getUtilization() {
            if (totalCapacity <= 0) return 0.0;
            return (double) currentLoad / totalCapacity;
        }
    }
}
