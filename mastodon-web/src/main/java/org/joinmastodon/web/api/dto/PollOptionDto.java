package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PollOptionDto(
        @JsonProperty("title") String title,
        @JsonProperty("votes_count") Integer votesCount) {
}
