package org.joinmastodon.resources.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for resource allocation.
 */
@Validated
@ConfigurationProperties(prefix = "mastodon.resources")
public class ResourceProperties {

    /**
     * CPU configuration
     */
    private CpuConfig cpu = new CpuConfig();

    /**
     * GPU configuration
     */
    private GpuConfig gpu = new GpuConfig();

    /**
     * Network configuration
     */
    private NetworkConfig network = new NetworkConfig();

    /**
     * Memory configuration
     */
    private MemoryConfig memory = new MemoryConfig();

    // Getters and setters
    public CpuConfig getCpu() {
        return cpu;
    }

    public void setCpu(CpuConfig cpu) {
        this.cpu = cpu;
    }

    public GpuConfig getGpu() {
        return gpu;
    }

    public void setGpu(GpuConfig gpu) {
        this.gpu = gpu;
    }

    public NetworkConfig getNetwork() {
        return network;
    }

    public void setNetwork(NetworkConfig network) {
        this.network = network;
    }

    public MemoryConfig getMemory() {
        return memory;
    }

    public void setMemory(MemoryConfig memory) {
        this.memory = memory;
    }

    /**
     * CPU configuration
     */
    public static class CpuConfig {
        private boolean enabled = true;
        private int cores = Runtime.getRuntime().availableProcessors();
        private List<Integer> affinity = List.of();
        private boolean hyperthreading = true;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCores() {
            return cores;
        }

        public void setCores(int cores) {
            this.cores = cores;
        }

        public List<Integer> getAffinity() {
            return affinity;
        }

        public void setAffinity(List<Integer> affinity) {
            this.affinity = affinity;
        }

        public boolean isHyperthreading() {
            return hyperthreading;
        }

        public void setHyperthreading(boolean hyperthreading) {
            this.hyperthreading = hyperthreading;
        }
    }

    /**
     * GPU configuration
     */
    public static class GpuConfig {
        private boolean enabled = false;
        private List<Integer> devices = List.of(0);
        private boolean mediaTranscoding = true;
        private String preferredApi = "CUDA";  // CUDA, OpenCL, or AUTO

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<Integer> getDevices() {
            return devices;
        }

        public void setDevices(List<Integer> devices) {
            this.devices = devices;
        }

        public boolean isMediaTranscoding() {
            return mediaTranscoding;
        }

        public void setMediaTranscoding(boolean mediaTranscoding) {
            this.mediaTranscoding = mediaTranscoding;
        }

        public String getPreferredApi() {
            return preferredApi;
        }

        public void setPreferredApi(String preferredApi) {
            this.preferredApi = preferredApi;
        }
    }

    /**
     * Network configuration
     */
    public static class NetworkConfig {
        private List<AdapterConfig> adapters = List.of();
        private int connectionTimeout = 30000;
        private int readTimeout = 60000;
        private int maxConnections = 200;

        // Getters and setters
        public List<AdapterConfig> getAdapters() {
            return adapters;
        }

        public void setAdapters(List<AdapterConfig> adapters) {
            this.adapters = adapters;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }

    /**
     * Network adapter configuration
     */
    public static class AdapterConfig {
        private String name;
        private int priority = 1;
        private String bandwidthLimit;
        private boolean enabled = true;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getBandwidthLimit() {
            return bandwidthLimit;
        }

        public void setBandwidthLimit(String bandwidthLimit) {
            this.bandwidthLimit = bandwidthLimit;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Memory configuration
     */
    public static class MemoryConfig {
        private String maxHeap = "4G";
        private String mediaCache = "2G";
        private boolean aggressiveGc = false;
        private int gcInterval = 60;

        // Getters and setters
        public String getMaxHeap() {
            return maxHeap;
        }

        public void setMaxHeap(String maxHeap) {
            this.maxHeap = maxHeap;
        }

        public String getMediaCache() {
            return mediaCache;
        }

        public void setMediaCache(String mediaCache) {
            this.mediaCache = mediaCache;
        }

        public boolean isAggressiveGc() {
            return aggressiveGc;
        }

        public void setAggressiveGc(boolean aggressiveGc) {
            this.aggressiveGc = aggressiveGc;
        }

        public int getGcInterval() {
            return gcInterval;
        }

        public void setGcInterval(int gcInterval) {
            this.gcInterval = gcInterval;
        }
    }
}
