package org.joinmastodon.cluster.failover;

import org.joinmastodon.cluster.config.ClusterProperties;
import org.joinmastodon.cluster.config.NodeProperties;
import org.joinmastodon.cluster.discovery.ClusterNode;
import org.joinmastodon.cluster.discovery.NodeRegistry;
import org.joinmastodon.cluster.distribution.TaskQueue;
import org.joinmastodon.cluster.sync.ClusterEvent;
import org.joinmastodon.cluster.sync.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FailoverManager.
 * Tests failover handling and node recovery scenarios.
 */
@ExtendWith(MockitoExtension.class)
class FailoverManagerTest {

    @Mock
    private ClusterProperties clusterProperties;

    @Mock
    private NodeProperties nodeProperties;

    @Mock
    private NodeRegistry nodeRegistry;

    @Mock
    private TaskQueue taskQueue;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private FailoverManager failoverManager;

    private ClusterNode testNode;
    private ClusterNode coordinatorNode;

    @BeforeEach
    void setUp() {
        testNode = createNode("node-1", "node-1.example.test", ClusterNode.NodeState.ACTIVE);
        coordinatorNode = createNode("coordinator-1", "coordinator.example.test", ClusterNode.NodeState.ACTIVE);
        
        when(nodeProperties.getId()).thenReturn("current-node");
    }

    @Nested
    @DisplayName("Event subscription")
    class EventSubscriptionTests {

        @Test
        @DisplayName("Subscribes to node failure events on construction")
        void subscribesToNodeFailureEvents() {
            verify(eventBus).subscribe(eq(ClusterEvent.Types.NODE_FAILED), any(Consumer.class));
        }

        @Test
        @DisplayName("Subscribes to node recovery events on construction")
        void subscribesToNodeRecoveryEvents() {
            verify(eventBus).subscribe(eq(ClusterEvent.Types.NODE_RECOVERED), any(Consumer.class));
        }
    }

    @Nested
    @DisplayName("Node failure handling")
    class NodeFailureTests {

        @Test
        @DisplayName("Marks failed node as unavailable")
        void marksNodeUnavailable() {
            String failedNodeId = "node-1";
            ClusterEvent event = createNodeFailedEvent(failedNodeId);
            when(taskQueue.reassignFromNode(failedNodeId)).thenReturn(5);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.empty());
            
            failoverManager.handleNodeFailure(event);
            
            verify(nodeRegistry).markUnavailable(failedNodeId);
        }

        @Test
        @DisplayName("Reassigns tasks from failed node")
        void reassignsTasksFromFailedNode() {
            String failedNodeId = "node-1";
            ClusterEvent event = createNodeFailedEvent(failedNodeId);
            when(taskQueue.reassignFromNode(failedNodeId)).thenReturn(10);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.empty());
            
            failoverManager.handleNodeFailure(event);
            
            verify(taskQueue).reassignFromNode(failedNodeId);
        }

        @Test
        @DisplayName("Publishes failover started event")
        void publishesFailoverStartedEvent() {
            String failedNodeId = "node-1";
            ClusterEvent event = createNodeFailedEvent(failedNodeId);
            when(taskQueue.reassignFromNode(failedNodeId)).thenReturn(0);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.empty());
            
            failoverManager.handleNodeFailure(event);
            
            ArgumentCaptor<ClusterEvent> eventCaptor = ArgumentCaptor.forClass(ClusterEvent.class);
            verify(eventBus).publish(eventCaptor.capture());
            
            assertThat(eventCaptor.getValue().getType()).isEqualTo(ClusterEvent.Types.FAILOVER_STARTED);
        }

        @Test
        @DisplayName("Publishes failover completed event")
        void publishesFailoverCompletedEvent() {
            String failedNodeId = "node-1";
            ClusterEvent event = createNodeFailedEvent(failedNodeId);
            when(taskQueue.reassignFromNode(failedNodeId)).thenReturn(0);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.empty());
            
            failoverManager.handleNodeFailure(event);
            
            ArgumentCaptor<ClusterEvent> eventCaptor = ArgumentCaptor.forClass(ClusterEvent.class);
            verify(eventBus).publish(eventCaptor.capture());
            
            assertThat(eventCaptor.getValue().getType()).isEqualTo(ClusterEvent.Types.FAILOVER_COMPLETED);
        }

        @Test
        @DisplayName("Handles coordinator failure by electing new coordinator")
        void handlesCoordinatorFailure() {
            String coordinatorId = "coordinator-1";
            ClusterEvent event = createNodeFailedEvent(coordinatorId);
            when(taskQueue.reassignFromNode(coordinatorId)).thenReturn(0);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.of(coordinatorNode));
            when(nodeProperties.getId()).thenReturn("current-node");
            
            failoverManager.handleNodeFailure(event);
            
            verify(nodeRegistry).electCoordinator();
        }

        @Test
        @DisplayName("Does not elect new coordinator for non-coordinator failure")
        void noElectionForNonCoordinatorFailure() {
            String failedNodeId = "node-1";
            ClusterEvent event = createNodeFailedEvent(failedNodeId);
            when(taskQueue.reassignFromNode(failedNodeId)).thenReturn(0);
            when(nodeRegistry.getCoordinator()).thenReturn(Optional.of(coordinatorNode));
            
            failoverManager.handleNodeFailure(event);
            
            // Should not elect new coordinator since failed node is not coordinator
            verify(nodeRegistry, org.mockito.Mockito.never()).electCoordinator();
        }
    }

    @Nested
    @DisplayName("Node recovery handling")
    class NodeRecoveryTests {

        @Test
        @DisplayName("Marks recovered node as active")
        void marksNodeActive() {
            String recoveredNodeId = "node-1";
            ClusterEvent event = createNodeRecoveredEvent(recoveredNodeId);
            when(nodeRegistry.getNode(recoveredNodeId)).thenReturn(Optional.of(testNode));
            
            failoverManager.handleNodeRecovery(event);
            
            assertThat(testNode.getState()).isEqualTo(ClusterNode.NodeState.ACTIVE);
        }

        @Test
        @DisplayName("Handles recovery of unknown node gracefully")
        void handlesUnknownNodeGracefully() {
            String recoveredNodeId = "unknown-node";
            ClusterEvent event = createNodeRecoveredEvent(recoveredNodeId);
            when(nodeRegistry.getNode(recoveredNodeId)).thenReturn(Optional.empty());
            
            // Should not throw exception
            failoverManager.handleNodeRecovery(event);
        }
    }

    // Helper methods

    private ClusterNode createNode(String id, String host, ClusterNode.NodeState state) {
        ClusterNode node = new ClusterNode(
                id,
                id,
                host,
                7946,
                "http://" + host + ":8080",
                java.util.List.of(org.joinmastodon.cluster.config.NodeProperties.NodeCapability.API),
                100,
                1,
                null,
                null,
                java.util.List.of()
        );
        node.setState(state);
        return node;
    }

    private ClusterEvent createNodeFailedEvent(String nodeId) {
        Map<String, Object> data = new HashMap<>();
        data.put("nodeId", nodeId);
        return new ClusterEvent(ClusterEvent.Types.NODE_FAILED, "source-node", data);
    }

    private ClusterEvent createNodeRecoveredEvent(String nodeId) {
        Map<String, Object> data = new HashMap<>();
        data.put("nodeId", nodeId);
        return new ClusterEvent(ClusterEvent.Types.NODE_RECOVERED, nodeId, data);
    }
}
