package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record PollDto(
        @JsonProperty("id") String id,
        @JsonProperty("expires_at") Instant expiresAt,
        @JsonProperty("expired") boolean expired,
        @JsonProperty("multiple") boolean multiple,
        @JsonProperty("votes_count") int votesCount,
        @JsonProperty("voters_count") Integer votersCount,
        @JsonProperty("voted") Boolean voted,
        @JsonProperty("options") List<PollOptionDto> options) {
}
