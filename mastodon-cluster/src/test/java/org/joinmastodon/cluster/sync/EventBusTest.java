package org.joinmastodon.cluster.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EventBus.
 * Tests event publishing, subscription, and history management.
 */
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @Nested
    @DisplayName("Subscription management")
    class SubscriptionTests {

        @Test
        @DisplayName("Subscribe to event type")
        void subscribeToEventType() {
            AtomicInteger callCount = new AtomicInteger(0);
            
            eventBus.subscribe("test.event", e -> callCount.incrementAndGet());
            
            assertThat(eventBus.getSubscriberCount("test.event")).isEqualTo(1);
        }

        @Test
        @DisplayName("Multiple subscribers for same event type")
        void multipleSubscribersForSameType() {
            eventBus.subscribe("test.event", e -> {});
            eventBus.subscribe("test.event", e -> {});
            eventBus.subscribe("test.event", e -> {});
            
            assertThat(eventBus.getSubscriberCount("test.event")).isEqualTo(3);
        }

        @Test
        @DisplayName("Unsubscribe from event type")
        void unsubscribeFromEventType() {
            EventBus.Subscription subscription = eventBus.subscribe("test.event", e -> {});
            
            assertThat(eventBus.getSubscriberCount("test.event")).isEqualTo(1);
            
            subscription.cancel();
            
            assertThat(eventBus.getSubscriberCount("test.event")).isEqualTo(0);
        }

        @Test
        @DisplayName("Subscription is active after subscribe")
        void subscriptionIsActiveAfterSubscribe() {
            EventBus.Subscription subscription = eventBus.subscribe("test.event", e -> {});
            
            assertThat(subscription.isActive()).isTrue();
        }

        @Test
        @DisplayName("Subscription is inactive after cancel")
        void subscriptionIsInactiveAfterCancel() {
            EventBus.Subscription subscription = eventBus.subscribe("test.event", e -> {});
            
            subscription.cancel();
            
            assertThat(subscription.isActive()).isFalse();
        }

        @Test
        @DisplayName("Double cancel is safe")
        void doubleCancelIsSafe() {
            EventBus.Subscription subscription = eventBus.subscribe("test.event", e -> {});
            
            subscription.cancel();
            subscription.cancel(); // Should not throw
            
            assertThat(subscription.isActive()).isFalse();
        }

        @Test
        @DisplayName("Subscribe to all events with wildcard")
        void subscribeToAllEvents() {
            AtomicInteger callCount = new AtomicInteger(0);
            
            eventBus.subscribeAll(e -> callCount.incrementAndGet());
            
            eventBus.publish(createEvent("event.type1"));
            eventBus.publish(createEvent("event.type2"));
            
            assertThat(callCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Event publishing")
    class PublishingTests {

        @Test
        @DisplayName("Publish event to subscriber")
        void publishEventToSubscriber() {
            AtomicInteger receivedCount = new AtomicInteger(0);
            
            eventBus.subscribe("test.event", e -> receivedCount.incrementAndGet());
            
            eventBus.publish(createEvent("test.event"));
            
            assertThat(receivedCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Event contains correct data")
        void eventContainsCorrectData() {
            final ClusterEvent[] receivedEvent = new ClusterEvent[1];
            
            eventBus.subscribe("test.event", e -> receivedEvent[0] = e);
            
            ClusterEvent sentEvent = createEvent("test.event", "node-1", Map.of("key", "value"));
            eventBus.publish(sentEvent);
            
            assertThat(receivedEvent[0]).isNotNull();
            assertThat(receivedEvent[0].getType()).isEqualTo("test.event");
            assertThat(receivedEvent[0].getSourceNodeId()).isEqualTo("node-1");
            assertThat(receivedEvent[0].getData()).containsEntry("key", "value");
        }

        @Test
        @DisplayName("Publish to multiple subscribers")
        void publishToMultipleSubscribers() {
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);
            
            eventBus.subscribe("test.event", e -> count1.incrementAndGet());
            eventBus.subscribe("test.event", e -> count2.incrementAndGet());
            
            eventBus.publish(createEvent("test.event"));
            
            assertThat(count1.get()).isEqualTo(1);
            assertThat(count2.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("No subscribers for event type does not throw")
        void noSubscribersDoesNotThrow() {
            eventBus.publish(createEvent("unregistered.event"));
            // Should not throw
        }

        @Test
        @DisplayName("Handler exception does not prevent other handlers")
        void handlerExceptionDoesNotPreventOtherHandlers() {
            AtomicInteger successfulCount = new AtomicInteger(0);
            
            eventBus.subscribe("test.event", e -> {
                throw new RuntimeException("Test exception");
            });
            eventBus.subscribe("test.event", e -> successfulCount.incrementAndGet());
            
            eventBus.publish(createEvent("test.event"));
            
            assertThat(successfulCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Event history")
    class HistoryTests {

        @Test
        @DisplayName("Events are stored in history")
        void eventsStoredInHistory() {
            eventBus.publish(createEvent("event.1"));
            eventBus.publish(createEvent("event.2"));
            
            List<ClusterEvent> history = eventBus.getRecentEvents(10);
            
            assertThat(history).hasSize(2);
        }

        @Test
        @DisplayName("Get recent events with limit")
        void getRecentEventsWithLimit() {
            for (int i = 0; i < 5; i++) {
                eventBus.publish(createEvent("event." + i));
            }
            
            List<ClusterEvent> history = eventBus.getRecentEvents(3);
            
            assertThat(history).hasSize(3);
        }

        @Test
        @DisplayName("Get events by type")
        void getEventsByType() {
            eventBus.publish(createEvent("type.a"));
            eventBus.publish(createEvent("type.b"));
            eventBus.publish(createEvent("type.a"));
            
            List<ClusterEvent> typeAEvents = eventBus.getEventsByType("type.a", 10);
            
            assertThat(typeAEvents).hasSize(2);
        }

        @Test
        @DisplayName("Get events by type with max count")
        void getEventsByTypeWithMaxCount() {
            for (int i = 0; i < 5; i++) {
                eventBus.publish(createEvent("type.a"));
            }
            
            List<ClusterEvent> events = eventBus.getEventsByType("type.a", 3);
            
            assertThat(events).hasSize(3);
        }

        @Test
        @DisplayName("Clear history")
        void clearHistory() {
            eventBus.publish(createEvent("event.1"));
            eventBus.publish(createEvent("event.2"));
            
            eventBus.clearHistory();
            
            List<ClusterEvent> history = eventBus.getRecentEvents(10);
            assertThat(history).isEmpty();
        }
    }

    @Nested
    @DisplayName("Async publishing")
    class AsyncPublishingTests {

        @Test
        @DisplayName("Publish async does not block")
        void publishAsyncDoesNotBlock() throws InterruptedException {
            AtomicInteger receivedCount = new AtomicInteger(0);
            
            eventBus.subscribe("test.event", e -> receivedCount.incrementAndGet());
            
            eventBus.publishAsync(createEvent("test.event"));
            
            // Wait for async execution
            Thread.sleep(100);
            
            assertThat(receivedCount.get()).isEqualTo(1);
        }
    }

    // Helper methods

    private ClusterEvent createEvent(String type) {
        return createEvent(type, "test-node", new HashMap<>());
    }

    private ClusterEvent createEvent(String type, String sourceNodeId, Map<String, Object> data) {
        return new ClusterEvent(type, sourceNodeId, data);
    }
}
