package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TagDto(
        @JsonProperty("name") String name,
        @JsonProperty("url") String url) {
}
