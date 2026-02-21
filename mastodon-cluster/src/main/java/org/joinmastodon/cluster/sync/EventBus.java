package org.joinmastodon.cluster.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Cluster-wide event bus for inter-node communication.
 * Supports pub/sub pattern for event distribution.
 */
@Component
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private final Map<String, List<Consumer<ClusterEvent>>> subscribers = new ConcurrentHashMap<>();
    private final List<ClusterEvent> eventHistory = new CopyOnWriteArrayList<>();
    private final int maxHistorySize = 1000;

    /**
     * Subscribe to events of a specific type.
     */
    public Subscription subscribe(String eventType, Consumer<ClusterEvent> handler) {
        List<Consumer<ClusterEvent>> handlers = subscribers.computeIfAbsent(
                eventType, k -> new CopyOnWriteArrayList<>());
        handlers.add(handler);
        log.debug("Subscribed to event type: {}", eventType);
        return new Subscription(this, eventType, handler);
    }

    /**
     * Subscribe to all events.
     */
    public Subscription subscribeAll(Consumer<ClusterEvent> handler) {
        return subscribe("*", handler);
    }

    /**
     * Unsubscribe from events.
     */
    public void unsubscribe(String eventType, Consumer<ClusterEvent> handler) {
        List<Consumer<ClusterEvent>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
            log.debug("Unsubscribed from event type: {}", eventType);
        }
    }

    /**
     * Publish an event to all subscribers.
     */
    public void publish(ClusterEvent event) {
        log.debug("Publishing event: {} from node {}", event.getType(), event.getSourceNodeId());
        
        // Store in history
        addToHistory(event);

        // Notify specific subscribers
        List<Consumer<ClusterEvent>> handlers = subscribers.get(event.getType());
        if (handlers != null) {
            for (Consumer<ClusterEvent> handler : handlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    log.error("Error handling event {}: {}", event.getType(), e.getMessage(), e);
                }
            }
        }

        // Notify wildcard subscribers
        List<Consumer<ClusterEvent>> wildcardHandlers = subscribers.get("*");
        if (wildcardHandlers != null) {
            for (Consumer<ClusterEvent> handler : wildcardHandlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    log.error("Error handling event {} (wildcard): {}", event.getType(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Publish an event asynchronously.
     */
    public void publishAsync(ClusterEvent event) {
        Thread.ofVirtual().start(() -> publish(event));
    }

    /**
     * Get recent events from history.
     */
    public List<ClusterEvent> getRecentEvents(int count) {
        int size = eventHistory.size();
        if (count >= size) {
            return new ArrayList<>(eventHistory);
        }
        return new ArrayList<>(eventHistory.subList(size - count, size));
    }

    /**
     * Get events of a specific type from history.
     */
    public List<ClusterEvent> getEventsByType(String eventType, int maxCount) {
        List<ClusterEvent> result = new ArrayList<>();
        for (int i = eventHistory.size() - 1; i >= 0 && result.size() < maxCount; i--) {
            ClusterEvent event = eventHistory.get(i);
            if (eventType.equals(event.getType())) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Add event to history with size limit.
     */
    private void addToHistory(ClusterEvent event) {
        eventHistory.add(event);
        while (eventHistory.size() > maxHistorySize) {
            eventHistory.remove(0);
        }
    }

    /**
     * Clear event history.
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    /**
     * Get subscriber count for an event type.
     */
    public int getSubscriberCount(String eventType) {
        List<Consumer<ClusterEvent>> handlers = subscribers.get(eventType);
        return handlers != null ? handlers.size() : 0;
    }

    /**
     * Represents a subscription that can be cancelled.
     */
    public static class Subscription {
        private final EventBus eventBus;
        private final String eventType;
        private final Consumer<ClusterEvent> handler;
        private volatile boolean active = true;

        public Subscription(EventBus eventBus, String eventType, Consumer<ClusterEvent> handler) {
            this.eventBus = eventBus;
            this.eventType = eventType;
            this.handler = handler;
        }

        /**
         * Cancel this subscription.
         */
        public void cancel() {
            if (active) {
                eventBus.unsubscribe(eventType, handler);
                active = false;
            }
        }

        public boolean isActive() {
            return active;
        }
    }
}
