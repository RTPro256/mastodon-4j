package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MentionDto(
        @JsonProperty("id") String id,
        @JsonProperty("username") String username,
        @JsonProperty("acct") String acct,
        @JsonProperty("url") String url) {
}
