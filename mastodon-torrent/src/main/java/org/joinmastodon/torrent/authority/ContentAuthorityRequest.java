package org.joinmastodon.torrent.authority;

/**
 * Request object for creating a content authority record.
 */
public class ContentAuthorityRequest {

    private final String infohash;
    private final Long creatorAccountId;
    private final String creatorUsername;
    private final long createdAt;

    private ContentAuthorityRequest(Builder builder) {
        this.infohash = builder.infohash;
        this.creatorAccountId = builder.creatorAccountId;
        this.creatorUsername = builder.creatorUsername;
        this.createdAt = builder.createdAt;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String infohash;
        private Long creatorAccountId;
        private String creatorUsername;
        private long createdAt = System.currentTimeMillis();

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

        public ContentAuthorityRequest build() {
            if (infohash == null || infohash.isBlank()) {
                throw new IllegalArgumentException("Infohash is required");
            }
            if (creatorAccountId == null) {
                throw new IllegalArgumentException("Creator account ID is required");
            }
            if (creatorUsername == null || creatorUsername.isBlank()) {
                throw new IllegalArgumentException("Creator username is required");
            }
            return new ContentAuthorityRequest(this);
        }
    }
}
