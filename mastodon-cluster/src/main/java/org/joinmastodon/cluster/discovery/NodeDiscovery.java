package org.joinmastodon.cluster.discovery;

import org.joinmastodon.cluster.config.ClusterProperties;
import org.joinmastodon.cluster.config.NodeProperties;
import org.joinmastodon.cluster.config.NodeProperties.NodeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles node discovery and registration in the cluster.
 */
@Component
public class NodeDiscovery {

    private static final Logger log = LoggerFactory.getLogger(NodeDiscovery.class);

    private final ClusterProperties clusterProperties;
    private final NodeProperties nodeProperties;
    private final NodeRegistry nodeRegistry;

    public NodeDiscovery(ClusterProperties clusterProperties, NodeProperties nodeProperties, 
                         NodeRegistry nodeRegistry) {
        this.clusterProperties = clusterProperties;
        this.nodeProperties = nodeProperties;
        this.nodeRegistry = nodeRegistry;
    }

    /**
     * Initialize this node and join the cluster.
     */
    public void initialize() {
        if (!clusterProperties.isEnabled()) {
            log.info("Cluster mode is disabled, running as standalone");
            return;
        }

        // Generate node ID if not set
        if (nodeProperties.getId() == null || nodeProperties.getId().isBlank()) {
            nodeProperties.setId(generateNodeId());
        }

        // Set default host if not configured
        if (nodeProperties.getHost() == null || nodeProperties.getHost().isBlank()) {
            nodeProperties.setHost(getLocalHost());
        }

        // Register this node
        ClusterNode thisNode = createLocalNode();
        nodeRegistry.register(thisNode);

        // If this is a coordinator or hybrid, set as coordinator
        if (clusterProperties.getRole() == ClusterProperties.NodeRole.COORDINATOR ||
            clusterProperties.getRole() == ClusterProperties.NodeRole.HYBRID) {
            nodeRegistry.setCoordinator(nodeProperties.getId());
        }

        log.info("Node initialized: {} (role: {})", nodeProperties.getId(), clusterProperties.getRole());
    }

    /**
     * Discover other nodes in the cluster.
     */
    public void discoverNodes() {
        if (!clusterProperties.isEnabled()) {
            return;
        }

        ClusterProperties.DiscoveryType discoveryType = clusterProperties.getDiscovery().getType();
        log.debug("Discovering nodes via: {}", discoveryType);

        switch (discoveryType) {
            case STATIC:
                discoverStaticNodes();
                break;
            case CONSUL:
                discoverViaConsul();
                break;
            case ETCD:
                discoverViaEtcd();
                break;
            case MULTICAST:
                discoverViaMulticast();
                break;
            default:
                log.warn("Unknown discovery type: {}", discoveryType);
        }
    }

    /**
     * Discover nodes from static configuration.
     */
    private void discoverStaticNodes() {
        List<String> endpoints = clusterProperties.getDiscovery().getEndpoints();
        for (String endpoint : endpoints) {
            // Parse endpoint (format: host:port or just host)
            String host;
            int port = 7946;
            if (endpoint.contains(":")) {
                String[] parts = endpoint.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                host = endpoint;
            }

            // Create a remote node entry
            String nodeId = "remote-" + host + "-" + port;
            if (nodeRegistry.getNode(nodeId).isEmpty()) {
                ClusterNode remoteNode = new ClusterNode(
                        nodeId,
                        host,
                        host,
                        port,
                        "http://" + host + ":8080",
                        List.of(NodeCapability.API, NodeCapability.WORKER),
                        100,
                        1,
                        null,
                        null,
                        List.of()
                );
                remoteNode.setState(ClusterNode.NodeState.ACTIVE);
                nodeRegistry.register(remoteNode);
                log.info("Discovered static node: {}:{}", host, port);
            }
        }
    }

    /**
     * Discover nodes via Consul.
     */
    private void discoverViaConsul() {
        // TODO: Implement Consul discovery
        log.debug("Consul discovery not yet implemented");
    }

    /**
     * Discover nodes via etcd.
     */
    private void discoverViaEtcd() {
        // TODO: Implement etcd discovery
        log.debug("etcd discovery not yet implemented");
    }

    /**
     * Discover nodes via multicast.
     */
    private void discoverViaMulticast() {
        // TODO: Implement multicast discovery
        log.debug("Multicast discovery not yet implemented");
    }

    /**
     * Scheduled health check for nodes.
     */
    @Scheduled(fixedDelayString = "${mastodon.cluster.failover.heartbeat-interval:5000}")
    public void checkNodeHealth() {
        if (!clusterProperties.isEnabled()) {
            return;
        }

        Duration heartbeatTimeout = clusterProperties.getFailover().getHeartbeatInterval()
                .multipliedBy(clusterProperties.getFailover().getMaxMissedHeartbeats());
        Instant threshold = Instant.now().minus(heartbeatTimeout);

        for (ClusterNode node : nodeRegistry.getAllNodes()) {
            // Skip this node
            if (node.getId().equals(nodeProperties.getId())) {
                continue;
            }

            // Check if heartbeat is stale
            if (node.getLastHeartbeat() != null && node.getLastHeartbeat().isBefore(threshold)) {
                log.warn("Node {} has missed heartbeats, marking unavailable", node.getId());
                nodeRegistry.markUnavailable(node.getId());
            }
        }
    }

    /**
     * Create a ClusterNode representing this instance.
     */
    private ClusterNode createLocalNode() {
        ClusterNode node = new ClusterNode(
                nodeProperties.getId(),
                nodeProperties.getName() != null ? nodeProperties.getName() : nodeProperties.getId(),
                nodeProperties.getHost(),
                nodeProperties.getPort(),
                nodeProperties.getApiEndpoint() != null ? 
                        nodeProperties.getApiEndpoint() : "http://" + nodeProperties.getHost() + ":8080",
                nodeProperties.getCapabilities(),
                nodeProperties.getMaxTasks(),
                nodeProperties.getWeight(),
                nodeProperties.getRegion(),
                nodeProperties.getZone(),
                nodeProperties.getTags()
        );
        node.setState(ClusterNode.NodeState.ACTIVE);
        return node;
    }

    /**
     * Generate a unique node ID.
     */
    private String generateNodeId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return hostName + "-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (UnknownHostException e) {
            return "node-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * Get the local host address.
     */
    private String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
