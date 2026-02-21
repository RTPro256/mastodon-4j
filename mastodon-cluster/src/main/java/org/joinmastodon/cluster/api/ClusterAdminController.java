package org.joinmastodon.cluster.api;

import org.joinmastodon.cluster.config.ClusterHealthIndicator;
import org.joinmastodon.cluster.discovery.ClusterNode;
import org.joinmastodon.cluster.discovery.NodeRegistry;
import org.joinmastodon.cluster.distribution.TaskQueue;
import org.joinmastodon.cluster.distribution.WorkloadDistributor;
import org.joinmastodon.cluster.failover.FailoverManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Admin API endpoints for cluster management.
 */
@RestController
@RequestMapping("/api/v2/admin/cluster")
public class ClusterAdminController {

    private final NodeRegistry nodeRegistry;
    private final TaskQueue taskQueue;
    private final WorkloadDistributor workloadDistributor;
    private final FailoverManager failoverManager;
    private final ClusterHealthIndicator healthIndicator;

    public ClusterAdminController(NodeRegistry nodeRegistry, TaskQueue taskQueue,
                                 WorkloadDistributor workloadDistributor,
                                 FailoverManager failoverManager,
                                 ClusterHealthIndicator healthIndicator) {
        this.nodeRegistry = nodeRegistry;
        this.taskQueue = taskQueue;
        this.workloadDistributor = workloadDistributor;
        this.failoverManager = failoverManager;
        this.healthIndicator = healthIndicator;
    }

    /**
     * Get cluster status.
     */
    @GetMapping("/status")
    public ResponseEntity<ClusterStatusResponse> getStatus() {
        NodeRegistry.ClusterStats stats = nodeRegistry.getStats();
        TaskQueue.QueueStats queueStats = taskQueue.getStats();
        WorkloadDistributor.DistributionStats distStats = workloadDistributor.getStats();
        FailoverManager.FailoverStatus failoverStatus = failoverManager.getStatus();

        return ResponseEntity.ok(new ClusterStatusResponse(
                stats.getTotalNodes(),
                stats.getActiveNodes(),
                stats.getUnavailableNodes(),
                stats.getUtilization(),
                queueStats.getPending(),
                queueStats.getAssigned(),
                distStats.getAverageLoad(),
                failoverStatus.isCoordinator(),
                failoverStatus.isHealthy()
        ));
    }

    /**
     * Get all nodes in the cluster.
     */
    @GetMapping("/nodes")
    public ResponseEntity<Collection<ClusterNode>> getNodes() {
        return ResponseEntity.ok(nodeRegistry.getAllNodes());
    }

    /**
     * Get a specific node.
     */
    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<ClusterNode> getNode(@PathVariable String nodeId) {
        return nodeRegistry.getNode(nodeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get task queue statistics.
     */
    @GetMapping("/queue")
    public ResponseEntity<TaskQueue.QueueStats> getQueueStats() {
        return ResponseEntity.ok(taskQueue.getStats());
    }

    /**
     * Get distribution statistics.
     */
    @GetMapping("/distribution")
    public ResponseEntity<WorkloadDistributor.DistributionStats> getDistributionStats() {
        return ResponseEntity.ok(workloadDistributor.getStats());
    }

    /**
     * Get failover status.
     */
    @GetMapping("/failover")
    public ResponseEntity<FailoverManager.FailoverStatus> getFailoverStatus() {
        return ResponseEntity.ok(failoverManager.getStatus());
    }

    /**
     * Mark a node as unavailable.
     */
    @PostMapping("/nodes/{nodeId}/unavailable")
    public ResponseEntity<Void> markNodeUnavailable(@PathVariable String nodeId) {
        nodeRegistry.markUnavailable(nodeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reassign tasks from a node.
     */
    @PostMapping("/nodes/{nodeId}/reassign")
    public ResponseEntity<Map<String, Integer>> reassignTasks(@PathVariable String nodeId) {
        int reassigned = taskQueue.reassignFromNode(nodeId);
        return ResponseEntity.ok(Map.of("reassigned", reassigned));
    }

    /**
     * Cluster status response.
     */
    public record ClusterStatusResponse(
            int totalNodes,
            int activeNodes,
            int unavailableNodes,
            double utilization,
            int pendingTasks,
            int assignedTasks,
            double averageLoad,
            boolean isCoordinator,
            boolean isHealthy
    ) {}
}
