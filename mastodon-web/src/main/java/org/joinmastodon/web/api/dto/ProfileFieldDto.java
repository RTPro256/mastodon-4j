package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ProfileFieldDto(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value,
        @JsonProperty("verified_at") Instant verifiedAt) {
}
