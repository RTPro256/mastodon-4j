package org.joinmastodon.cluster.sync;

import org.joinmastodon.cluster.config.ClusterProperties;
import org.joinmastodon.cluster.config.NodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Synchronizes shared state across cluster nodes.
 * Uses a simple last-write-wins conflict resolution strategy.
 */
@Component
public class StateSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(StateSynchronizer.class);

    private final ClusterProperties clusterProperties;
    private final NodeProperties nodeProperties;
    private final EventBus eventBus;

    // Local state store
    private final Map<String, StateEntry> stateStore = new ConcurrentHashMap<>();
    
    // Vector clock for ordering
    private final Map<String, Long> vectorClock = new ConcurrentHashMap<>();

    public StateSynchronizer(ClusterProperties clusterProperties, NodeProperties nodeProperties,
                            EventBus eventBus) {
        this.clusterProperties = clusterProperties;
        this.nodeProperties = nodeProperties;
        this.eventBus = eventBus;

        // Subscribe to state updates from other nodes
        eventBus.subscribe(ClusterEvent.Types.STATE_UPDATE, this::handleStateUpdate);
    }

    /**
     * Put a value in the state store.
     */
    public void put(String key, Object value) {
        String nodeId = nodeProperties.getId();
        long version = incrementVectorClock(nodeId);
        Instant now = Instant.now();

        StateEntry entry = new StateEntry(key, value, nodeId, version, now);
        stateStore.put(key, entry);

        // Broadcast update
        broadcastUpdate(entry);
        log.debug("State updated: {} = {} (v{})", key, value, version);
    }

    /**
     * Get a value from the state store.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        StateEntry entry = stateStore.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of((T) entry.getValue());
    }

    /**
     * Get a value with a default.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        StateEntry entry = stateStore.get(key);
        if (entry == null) {
            return defaultValue;
        }
        return (T) entry.getValue();
    }

    /**
     * Remove a value from the state store.
     */
    public void remove(String key) {
        StateEntry removed = stateStore.remove(key);
        if (removed != null) {
            String nodeId = nodeProperties.getId();
            long version = incrementVectorClock(nodeId);
            
            // Broadcast removal
            Map<String, Object> data = new HashMap<>();
            data.put("key", key);
            data.put("removed", true);
            data.put("version", version);
            data.put("source", nodeId);
            
            eventBus.publish(new ClusterEvent(ClusterEvent.Types.STATE_UPDATE, nodeId, data));
            log.debug("State removed: {} (v{})", key, version);
        }
    }

    /**
     * Check if a key exists.
     */
    public boolean containsKey(String key) {
        return stateStore.containsKey(key);
    }

    /**
     * Get all keys.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(stateStore.keySet());
    }

    /**
     * Get all entries.
     */
    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>();
        stateStore.forEach((key, entry) -> result.put(key, entry.getValue()));
        return result;
    }

    /**
     * Handle state update from another node.
     */
    private void handleStateUpdate(ClusterEvent event) {
        Map<String, Object> data = event.getData();
        String key = (String) data.get("key");
        Boolean removed = (Boolean) data.get("removed");
        Long version = (Long) data.get("version");
        String source = event.getSourceNodeId();

        if (source.equals(nodeProperties.getId())) {
            // Ignore our own updates
            return;
        }

        if (Boolean.TRUE.equals(removed)) {
            stateStore.remove(key);
            log.debug("State removed from {}: {}", source, key);
            return;
        }

        Object value = data.get("value");
        Instant timestamp = event.getTimestamp();

        // Check for conflicts
        StateEntry existing = stateStore.get(key);
        if (existing != null) {
            // Last-write-wins
            if (timestamp.isBefore(existing.getTimestamp())) {
                log.debug("Ignoring stale update for key: {}", key);
                return;
            }
        }

        StateEntry entry = new StateEntry(key, value, source, version, timestamp);
        stateStore.put(key, entry);
        updateVectorClock(source, version);
        log.debug("State synced from {}: {} = {}", source, key, value);
    }

    /**
     * Broadcast a state update to other nodes.
     */
    private void broadcastUpdate(StateEntry entry) {
        Map<String, Object> data = new HashMap<>();
        data.put("key", entry.getKey());
        data.put("value", entry.getValue());
        data.put("version", entry.getVersion());
        
        eventBus.publish(new ClusterEvent(
                ClusterEvent.Types.STATE_UPDATE, 
                nodeProperties.getId(), 
                data));
    }

    /**
     * Increment the vector clock for this node.
     */
    private long incrementVectorClock(String nodeId) {
        return vectorClock.merge(nodeId, 1L, Long::sum);
    }

    /**
     * Update vector clock from received update.
     */
    private void updateVectorClock(String nodeId, long version) {
        vectorClock.merge(nodeId, version, Math::max);
    }

    /**
     * Get the current vector clock state.
     */
    public Map<String, Long> getVectorClock() {
        return Collections.unmodifiableMap(vectorClock);
    }

    /**
     * Scheduled sync task.
     */
    @Scheduled(fixedDelayString = "${mastodon.cluster.replication.sync-interval:5000}")
    public void syncState() {
        if (!clusterProperties.isEnabled()) {
            return;
        }
        // State is synced via events, this is just for periodic consistency checks
        log.trace("State sync check - {} entries", stateStore.size());
    }

    /**
     * State entry with metadata.
     */
    public static class StateEntry {
        private final String key;
        private final Object value;
        private final String sourceNode;
        private final long version;
        private final Instant timestamp;

        public StateEntry(String key, Object value, String sourceNode, long version, Instant timestamp) {
            this.key = key;
            this.value = value;
            this.sourceNode = sourceNode;
            this.version = version;
            this.timestamp = timestamp;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public String getSourceNode() {
            return sourceNode;
        }

        public long getVersion() {
            return version;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
