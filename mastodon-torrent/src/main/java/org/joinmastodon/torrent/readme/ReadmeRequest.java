package org.joinmastodon.torrent.readme;

/**
 * Request object for generating a torrent README.md file.
 */
public class ReadmeRequest {

    private final String contentTitle;
    private final String contentDescription;
    private final String contentType;
    private final long createdAt;
    private final String creatorUsername;
    private final String creatorDisplayName;
    private final Long creatorAccountId;
    private final String infohash;
    private final Long totalSize;
    private final String license;

    private ReadmeRequest(Builder builder) {
        this.contentTitle = builder.contentTitle;
        this.contentDescription = builder.contentDescription;
        this.contentType = builder.contentType;
        this.createdAt = builder.createdAt;
        this.creatorUsername = builder.creatorUsername;
        this.creatorDisplayName = builder.creatorDisplayName;
        this.creatorAccountId = builder.creatorAccountId;
        this.infohash = builder.infohash;
        this.totalSize = builder.totalSize;
        this.license = builder.license;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public String getContentType() {
        return contentType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public String getCreatorDisplayName() {
        return creatorDisplayName;
    }

    public Long getCreatorAccountId() {
        return creatorAccountId;
    }

    public String getInfohash() {
        return infohash;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public String getLicense() {
        return license;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String contentTitle;
        private String contentDescription;
        private String contentType = "Media Attachment";
        private long createdAt = System.currentTimeMillis();
        private String creatorUsername;
        private String creatorDisplayName;
        private Long creatorAccountId;
        private String infohash;
        private Long totalSize;
        private String license;

        public Builder contentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder contentDescription(String contentDescription) {
            this.contentDescription = contentDescription;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder creatorUsername(String creatorUsername) {
            this.creatorUsername = creatorUsername;
            return this;
        }

        public Builder creatorDisplayName(String creatorDisplayName) {
            this.creatorDisplayName = creatorDisplayName;
            return this;
        }

        public Builder creatorAccountId(Long creatorAccountId) {
            this.creatorAccountId = creatorAccountId;
            return this;
        }

        public Builder infohash(String infohash) {
            this.infohash = infohash;
            return this;
        }

        public Builder totalSize(Long totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        public Builder license(String license) {
            this.license = license;
            return this;
        }

        public ReadmeRequest build() {
            if (contentTitle == null || contentTitle.isBlank()) {
                throw new IllegalArgumentException("Content title is required");
            }
            if (creatorUsername == null || creatorUsername.isBlank()) {
                throw new IllegalArgumentException("Creator username is required");
            }
            if (infohash == null || infohash.isBlank()) {
                throw new IllegalArgumentException("Infohash is required");
            }
            return new ReadmeRequest(this);
        }
    }
}
