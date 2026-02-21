package org.joinmastodon.cluster.distribution;

import org.joinmastodon.cluster.config.NodeProperties.NodeCapability;
import org.joinmastodon.cluster.discovery.ClusterNode;
import org.joinmastodon.cluster.discovery.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Distributes workload across cluster nodes.
 * Implements weighted round-robin with capability-based routing.
 */
@Component
public class WorkloadDistributor {

    private static final Logger log = LoggerFactory.getLogger(WorkloadDistributor.class);

    private final NodeRegistry nodeRegistry;
    private final TaskQueue taskQueue;
    private final Map<String, AtomicInteger> nodeCounters = new ConcurrentHashMap<>();

    public WorkloadDistributor(NodeRegistry nodeRegistry, TaskQueue taskQueue) {
        this.nodeRegistry = nodeRegistry;
        this.taskQueue = taskQueue;
    }

    /**
     * Select the best node for a task.
     */
    public Optional<ClusterNode> selectNode(DistributedTask task) {
        return selectNode(task, null);
    }

    /**
     * Select the best node for a task with a specific capability requirement.
     */
    public Optional<ClusterNode> selectNode(DistributedTask task, NodeCapability requiredCapability) {
        List<ClusterNode> candidates = getCandidates(requiredCapability);
        
        if (candidates.isEmpty()) {
            log.warn("No available nodes for task {} (capability: {})", task.getId(), requiredCapability);
            return Optional.empty();
        }

        // Sort by load factor (ascending)
        candidates.sort(Comparator.comparingDouble(ClusterNode::getLoadFactor));

        // Apply weighted selection
        ClusterNode selected = weightedSelect(candidates);
        log.debug("Selected node {} for task {} (load: {})", 
                selected.getId(), task.getId(), selected.getLoadFactor());
        
        return Optional.of(selected);
    }

    /**
     * Distribute pending tasks to available nodes.
     */
    public int distributePendingTasks() {
        int distributed = 0;
        
        for (ClusterNode node : nodeRegistry.getActiveNodes()) {
            // Check if node has capacity
            if (!node.isAvailable()) {
                continue;
            }

            // Get a task for this node
            Optional<DistributedTask> taskOpt = taskQueue.poll(node.getId());
            if (taskOpt.isPresent()) {
                DistributedTask task = taskOpt.get();
                assignTaskToNode(task, node);
                distributed++;
            }
        }

        return distributed;
    }

    /**
     * Assign a task to a specific node.
     */
    public void assignTaskToNode(DistributedTask task, ClusterNode node) {
        log.info("Assigning task {} to node {}", task.getId(), node.getId());
        // Update node's current task count
        node.setCurrentTasks(node.getCurrentTasks() + 1);
        // The task is already assigned in TaskQueue.poll()
    }

    /**
     * Get candidate nodes for task assignment.
     */
    private List<ClusterNode> getCandidates(NodeCapability requiredCapability) {
        if (requiredCapability != null) {
            return nodeRegistry.getNodesWithCapability(requiredCapability);
        }
        return nodeRegistry.getActiveNodes();
    }

    /**
     * Weighted selection based on node weight and current load.
     */
    private ClusterNode weightedSelect(List<ClusterNode> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // Calculate effective weights (weight * (1 - loadFactor))
        double[] effectiveWeights = new double[candidates.size()];
        double totalWeight = 0.0;

        for (int i = 0; i < candidates.size(); i++) {
            ClusterNode node = candidates.get(i);
            double loadFactor = node.getLoadFactor();
            effectiveWeights[i] = node.getWeight() * (1.0 - loadFactor);
            totalWeight += effectiveWeights[i];
        }

        // Normalize and select
        if (totalWeight <= 0) {
            // All nodes are at max capacity, pick least loaded
            return candidates.get(0);
        }

        double random = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < candidates.size(); i++) {
            cumulative += effectiveWeights[i];
            if (random <= cumulative) {
                return candidates.get(i);
            }
        }

        return candidates.get(candidates.size() - 1);
    }

    /**
     * Get the next node using round-robin.
     */
    public Optional<ClusterNode> roundRobin(NodeCapability capability) {
        List<ClusterNode> nodes = getCandidates(capability);
        if (nodes.isEmpty()) {
            return Optional.empty();
        }

        String key = capability != null ? capability.name() : "default";
        AtomicInteger counter = nodeCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement() % nodes.size());
        
        return Optional.of(nodes.get(index));
    }

    /**
     * Find the least loaded node.
     */
    public Optional<ClusterNode> leastLoaded(NodeCapability capability) {
        return getCandidates(capability).stream()
                .filter(ClusterNode::isAvailable)
                .min(Comparator.comparingDouble(ClusterNode::getLoadFactor));
    }

    /**
     * Find nodes in a specific region.
     */
    public List<ClusterNode> nodesInRegion(String region) {
        return nodeRegistry.getActiveNodes().stream()
                .filter(node -> region.equals(node.getRegion()))
                .toList();
    }

    /**
     * Find nodes with specific tags.
     */
    public List<ClusterNode> nodesWithTags(List<String> requiredTags) {
        return nodeRegistry.getActiveNodes().stream()
                .filter(node -> node.getTags().containsAll(requiredTags))
                .toList();
    }

    /**
     * Get distribution statistics.
     */
    public DistributionStats getStats() {
        List<ClusterNode> nodes = nodeRegistry.getActiveNodes();
        
        double avgLoad = nodes.stream()
                .mapToDouble(ClusterNode::getLoadFactor)
                .average()
                .orElse(0.0);
        
        double maxLoad = nodes.stream()
                .mapToDouble(ClusterNode::getLoadFactor)
                .max()
                .orElse(0.0);
        
        double minLoad = nodes.stream()
                .mapToDouble(ClusterNode::getLoadFactor)
                .min()
                .orElse(0.0);

        return new DistributionStats(nodes.size(), avgLoad, maxLoad, minLoad);
    }

    /**
     * Distribution statistics.
     */
    public static class DistributionStats {
        private final int activeNodes;
        private final double averageLoad;
        private final double maxLoad;
        private final double minLoad;

        public DistributionStats(int activeNodes, double averageLoad, 
                                double maxLoad, double minLoad) {
            this.activeNodes = activeNodes;
            this.averageLoad = averageLoad;
            this.maxLoad = maxLoad;
            this.minLoad = minLoad;
        }

        public int getActiveNodes() {
            return activeNodes;
        }

        public double getAverageLoad() {
            return averageLoad;
        }

        public double getMaxLoad() {
            return maxLoad;
        }

        public double getMinLoad() {
            return minLoad;
        }

        public double getLoadBalance() {
            if (maxLoad == 0) return 1.0;
            return minLoad / maxLoad;
        }
    }
}
