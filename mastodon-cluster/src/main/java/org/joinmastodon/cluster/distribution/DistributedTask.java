package org.joinmastodon.cluster.distribution;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a distributed task in the cluster.
 */
public class DistributedTask {

    private final String id;
    private final String type;
    private final Map<String, Object> payload;
    private final int priority;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final String assignedNodeId;
    private final TaskStatus status;
    private final int retryCount;
    private final int maxRetries;

    public DistributedTask(String type, Map<String, Object> payload, int priority, 
                          Instant expiresAt, int maxRetries) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
        this.priority = priority;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.assignedNodeId = null;
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = maxRetries;
    }

    private DistributedTask(String id, String type, Map<String, Object> payload, int priority,
                           Instant createdAt, Instant expiresAt, String assignedNodeId,
                           TaskStatus status, int retryCount, int maxRetries) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.priority = priority;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.assignedNodeId = assignedNodeId;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public int getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getAssignedNodeId() {
        return assignedNodeId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Check if the task has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if the task can be retried.
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    /**
     * Create a copy with a new status.
     */
    public DistributedTask withStatus(TaskStatus newStatus) {
        return new DistributedTask(id, type, payload, priority, createdAt, expiresAt,
                assignedNodeId, newStatus, retryCount, maxRetries);
    }

    /**
     * Create a copy assigned to a node.
     */
    public DistributedTask withAssignment(String nodeId) {
        return new DistributedTask(id, type, payload, priority, createdAt, expiresAt,
                nodeId, TaskStatus.ASSIGNED, retryCount, maxRetries);
    }

    /**
     * Create a copy with incremented retry count.
     */
    public DistributedTask withRetry() {
        return new DistributedTask(id, type, payload, priority, createdAt, expiresAt,
                null, TaskStatus.PENDING, retryCount + 1, maxRetries);
    }

    /**
     * Task status enumeration.
     */
    public enum TaskStatus {
        /**
         * Task is waiting to be assigned.
         */
        PENDING,

        /**
         * Task is assigned to a node.
         */
        ASSIGNED,

        /**
         * Task is currently running.
         */
        RUNNING,

        /**
         * Task completed successfully.
         */
        COMPLETED,

        /**
         * Task failed.
         */
        FAILED,

        /**
         * Task was cancelled.
         */
        CANCELLED
    }
}
