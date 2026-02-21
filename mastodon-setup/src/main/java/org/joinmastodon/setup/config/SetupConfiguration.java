package org.joinmastodon.setup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Configuration model for server setup.
 * Supports YAML/JSON script-based configuration and interactive setup.
 */
@Validated
@ConfigurationProperties(prefix = "mastodon.setup")
public class SetupConfiguration {

    /**
     * Server configuration
     */
    @NotNull
    private ServerConfig server = new ServerConfig();

    /**
     * Resource allocation configuration
     */
    private ResourceConfig resources = new ResourceConfig();

    /**
     * Content policy configuration
     */
    private ContentConfig content = new ContentConfig();

    /**
     * Federation index configuration
     */
    private FederationIndexConfig federationIndex = new FederationIndexConfig();

    /**
     * Setup mode: express, script, or interactive
     */
    private SetupMode mode = SetupMode.EXPRESS;

    // Getters and setters
    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public ResourceConfig getResources() {
        return resources;
    }

    public void setResources(ResourceConfig resources) {
        this.resources = resources;
    }

    public ContentConfig getContent() {
        return content;
    }

    public void setContent(ContentConfig content) {
        this.content = content;
    }

    public FederationIndexConfig getFederationIndex() {
        return federationIndex;
    }

    public void setFederationIndex(FederationIndexConfig federationIndex) {
        this.federationIndex = federationIndex;
    }

    public SetupMode getMode() {
        return mode;
    }

    public void setMode(SetupMode mode) {
        this.mode = mode;
    }

    /**
     * Server basic configuration
     */
    public static class ServerConfig {
        @NotBlank
        private String domain;

        @NotBlank
        private String name;

        private String description = "";
        private String adminEmail;
        private boolean registrationsOpen = true;
        private boolean approvalRequired = false;

        // Getters and setters
        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public boolean isRegistrationsOpen() {
            return registrationsOpen;
        }

        public void setRegistrationsOpen(boolean registrationsOpen) {
            this.registrationsOpen = registrationsOpen;
        }

        public boolean isApprovalRequired() {
            return approvalRequired;
        }

        public void setApprovalRequired(boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
        }
    }

    /**
     * Resource allocation configuration
     */
    public static class ResourceConfig {
        private int cpuCores = Runtime.getRuntime().availableProcessors();
        private int memoryGb = 4;
        private boolean gpuEnabled = false;
        private List<Integer> gpuDevices = List.of();
        private List<NetworkAdapterConfig> networkAdapters = List.of();

        // Getters and setters
        public int getCpuCores() {
            return cpuCores;
        }

        public void setCpuCores(int cpuCores) {
            this.cpuCores = cpuCores;
        }

        public int getMemoryGb() {
            return memoryGb;
        }

        public void setMemoryGb(int memoryGb) {
            this.memoryGb = memoryGb;
        }

        public boolean isGpuEnabled() {
            return gpuEnabled;
        }

        public void setGpuEnabled(boolean gpuEnabled) {
            this.gpuEnabled = gpuEnabled;
        }

        public List<Integer> getGpuDevices() {
            return gpuDevices;
        }

        public void setGpuDevices(List<Integer> gpuDevices) {
            this.gpuDevices = gpuDevices;
        }

        public List<NetworkAdapterConfig> getNetworkAdapters() {
            return networkAdapters;
        }

        public void setNetworkAdapters(List<NetworkAdapterConfig> networkAdapters) {
            this.networkAdapters = networkAdapters;
        }
    }

    /**
     * Network adapter configuration
     */
    public static class NetworkAdapterConfig {
        private String name;
        private int priority = 1;
        private String bandwidthLimit;

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
    }

    /**
     * Content policy configuration
     */
    public static class ContentConfig {
        private String defaultVisibility = "public";
        private boolean privateContentEnabled = false;
        private FederationMode federationMode = FederationMode.OPEN;

        // Getters and setters
        public String getDefaultVisibility() {
            return defaultVisibility;
        }

        public void setDefaultVisibility(String defaultVisibility) {
            this.defaultVisibility = defaultVisibility;
        }

        public boolean isPrivateContentEnabled() {
            return privateContentEnabled;
        }

        public void setPrivateContentEnabled(boolean privateContentEnabled) {
            this.privateContentEnabled = privateContentEnabled;
        }

        public FederationMode getFederationMode() {
            return federationMode;
        }

        public void setFederationMode(FederationMode federationMode) {
            this.federationMode = federationMode;
        }
    }

    /**
     * Federation index configuration
     */
    public static class FederationIndexConfig {
        private boolean enabled = true;
        private boolean shareRatings = true;
        private boolean shareRestrictions = false;
        private List<String> trustedPartners = List.of();

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isShareRatings() {
            return shareRatings;
        }

        public void setShareRatings(boolean shareRatings) {
            this.shareRatings = shareRatings;
        }

        public boolean isShareRestrictions() {
            return shareRestrictions;
        }

        public void setShareRestrictions(boolean shareRestrictions) {
            this.shareRestrictions = shareRestrictions;
        }

        public List<String> getTrustedPartners() {
            return trustedPartners;
        }

        public void setTrustedPartners(List<String> trustedPartners) {
            this.trustedPartners = trustedPartners;
        }
    }

    /**
     * Setup mode enumeration
     */
    public enum SetupMode {
        EXPRESS,      // Quick setup with defaults
        SCRIPT,       // YAML/JSON script-based
        INTERACTIVE   // CLI wizard
    }

    /**
     * Federation mode enumeration
     */
    public enum FederationMode {
        OPEN,      // Open federation
        LIMITED,   // Limited federation (approved instances only)
        CLOSED     // Closed federation (no federation)
    }
}
