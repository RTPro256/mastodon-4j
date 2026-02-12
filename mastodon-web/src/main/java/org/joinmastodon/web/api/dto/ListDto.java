package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ListDto(
        @JsonProperty("id") String id,
        @JsonProperty("title") String title) {
}
