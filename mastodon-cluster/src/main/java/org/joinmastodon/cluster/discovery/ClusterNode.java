package org.joinmastodon.cluster.discovery;

import org.joinmastodon.cluster.config.NodeProperties.NodeCapability;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents a node in the cluster.
 */
public class ClusterNode {

    private final String id;
    private final String name;
    private final String host;
    private final int port;
    private final String apiEndpoint;
    private final List<NodeCapability> capabilities;
    private final int maxTasks;
    private final int weight;
    private final String region;
    private final String zone;
    private final List<String> tags;

    private NodeState state;
    private Instant lastHeartbeat;
    private int currentTasks;
    private double loadAverage;

    public ClusterNode(String id, String name, String host, int port, String apiEndpoint,
                       List<NodeCapability> capabilities, int maxTasks, int weight,
                       String region, String zone, List<String> tags) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.apiEndpoint = apiEndpoint;
        this.capabilities = capabilities;
        this.maxTasks = maxTasks;
        this.weight = weight;
        this.region = region;
        this.zone = zone;
        this.tags = tags;
        this.state = NodeState.JOINING;
        this.currentTasks = 0;
        this.loadAverage = 0.0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public List<NodeCapability> getCapabilities() {
        return capabilities;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public int getWeight() {
        return weight;
    }

    public String getRegion() {
        return region;
    }

    public String getZone() {
        return zone;
    }

    public List<String> getTags() {
        return tags;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public int getCurrentTasks() {
        return currentTasks;
    }

    public void setCurrentTasks(int currentTasks) {
        this.currentTasks = currentTasks;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    /**
     * Calculate the load factor for this node (0.0 to 1.0).
     */
    public double getLoadFactor() {
        if (maxTasks <= 0) {
            return 1.0;
        }
        return (double) currentTasks / maxTasks;
    }

    /**
     * Check if this node has a specific capability.
     */
    public boolean hasCapability(NodeCapability capability) {
        return capabilities.contains(capability);
    }

    /**
     * Check if this node is available for new tasks.
     */
    public boolean isAvailable() {
        return state == NodeState.ACTIVE && currentTasks < maxTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterNode that = (ClusterNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", state=" + state +
                ", loadFactor=" + getLoadFactor() +
                '}';
    }

    /**
     * Node states.
     */
    public enum NodeState {
        /**
         * Node is joining the cluster.
         */
        JOINING,

        /**
         * Node is active and accepting tasks.
         */
        ACTIVE,

        /**
         * Node is draining tasks and will leave.
         */
        LEAVING,

        /**
         * Node is temporarily unavailable.
         */
        UNAVAILABLE,

        /**
         * Node has left the cluster.
         */
        LEFT
    }
}
