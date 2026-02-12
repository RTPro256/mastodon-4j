package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record FilterDto(
        @JsonProperty("id") String id,
        @JsonProperty("title") String title,
        @JsonProperty("context") List<String> context,
        @JsonProperty("expires_at") Instant expiresAt,
        @JsonProperty("filter_action") String filterAction,
        @JsonProperty("keywords") List<FilterKeywordDto> keywords) {
}
