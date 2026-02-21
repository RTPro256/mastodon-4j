package org.joinmastodon.cluster.sync;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an event in the cluster event bus.
 */
public class ClusterEvent {

    private final String id;
    private final String type;
    private final String sourceNodeId;
    private final Map<String, Object> data;
    private final Instant timestamp;

    public ClusterEvent(String type, String sourceNodeId, Map<String, Object> data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.sourceNodeId = sourceNodeId;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Common event types.
     */
    public static class Types {
        public static final String NODE_JOINED = "node.joined";
        public static final String NODE_LEFT = "node.left";
        public static final String NODE_FAILED = "node.failed";
        public static final String NODE_RECOVERED = "node.recovered";
        public static final String HEARTBEAT = "node.heartbeat";
        
        public static final String TASK_ASSIGNED = "task.assigned";
        public static final String TASK_COMPLETED = "task.completed";
        public static final String TASK_FAILED = "task.failed";
        
        public static final String STATE_UPDATE = "state.update";
        public static final String CACHE_INVALIDATE = "cache.invalidate";
        
        public static final String COORDINATOR_CHANGED = "coordinator.changed";
        public static final String FAILOVER_STARTED = "failover.started";
        public static final String FAILOVER_COMPLETED = "failover.completed";
    }
}
