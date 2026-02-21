package org.joinmastodon.torrent.authority;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Record representing a content authority verification.
 * 
 * Contains the cryptographic signature and metadata proving
 * that content originated from a specific user on this server.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentAuthorityRecord {

    private final String infohash;
    private final Long creatorAccountId;
    private final String creatorUsername;
    private final long createdAt;
    private final String serverName;
    private final String serverUrl;
    private final String signature;
    private final String signatureAlgorithm;
    private final String serverPublicKey;

    private ContentAuthorityRecord(Builder builder) {
        this.infohash = builder.infohash;
        this.creatorAccountId = builder.creatorAccountId;
        this.creatorUsername = builder.creatorUsername;
        this.createdAt = builder.createdAt;
        this.serverName = builder.serverName;
        this.serverUrl = builder.serverUrl;
        this.signature = builder.signature;
        this.signatureAlgorithm = builder.signatureAlgorithm;
        this.serverPublicKey = builder.serverPublicKey;
    }

    public String getInfohash() {
        return infohash;
    }

    public Long getCreatorAccountId() {
        return creatorAccountId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getSignature() {
        return signature;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    /**
     * Get the ISO 8601 formatted creation timestamp.
     */
    public String getCreatedAtIso() {
        return Instant.ofEpochMilli(createdAt).toString();
    }

    /**
     * Get the verification URL for this content.
     */
    public String getVerificationUrl() {
        return serverUrl + "/api/v1/torrents/authority/" + infohash;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String infohash;
        private Long creatorAccountId;
        private String creatorUsername;
        private long createdAt;
        private String serverName;
        private String serverUrl;
        private String signature;
        private String signatureAlgorithm;
        private String serverPublicKey;

        public Builder infohash(String infohash) {
            this.infohash = infohash;
            return this;
        }

        public Builder creatorAccountId(Long creatorAccountId) {
            this.creatorAccountId = creatorAccountId;
            return this;
        }

        public Builder creatorUsername(String creatorUsername) {
            this.creatorUsername = creatorUsername;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder signatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public Builder serverPublicKey(String serverPublicKey) {
            this.serverPublicKey = serverPublicKey;
            return this;
        }

        public ContentAuthorityRecord build() {
            return new ContentAuthorityRecord(this);
        }
    }
}
