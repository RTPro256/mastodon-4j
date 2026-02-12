package org.joinmastodon.federation.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mastodon.federation")
public class FederationProperties {
    private String baseUrl = "http://localhost:8080";
    private String domain = "localhost";
    private String keyId;
    private String publicKeyPem;
    private String privateKeyPem;
    private boolean requireSignatures = false;
    private Duration deliveryPollInterval = Duration.ofSeconds(5);
    private Duration deliveryLockTimeout = Duration.ofMinutes(5);
    private int deliveryBatchSize = 5;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public String getPrivateKeyPem() {
        return privateKeyPem;
    }

    public void setPrivateKeyPem(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }

    public boolean isRequireSignatures() {
        return requireSignatures;
    }

    public void setRequireSignatures(boolean requireSignatures) {
        this.requireSignatures = requireSignatures;
    }

    public Duration getDeliveryPollInterval() {
        return deliveryPollInterval;
    }

    public void setDeliveryPollInterval(Duration deliveryPollInterval) {
        this.deliveryPollInterval = deliveryPollInterval;
    }

    public Duration getDeliveryLockTimeout() {
        return deliveryLockTimeout;
    }

    public void setDeliveryLockTimeout(Duration deliveryLockTimeout) {
        this.deliveryLockTimeout = deliveryLockTimeout;
    }

    public int getDeliveryBatchSize() {
        return deliveryBatchSize;
    }

    public void setDeliveryBatchSize(int deliveryBatchSize) {
        this.deliveryBatchSize = deliveryBatchSize;
    }
}
