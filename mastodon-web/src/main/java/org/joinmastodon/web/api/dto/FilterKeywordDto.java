package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilterKeywordDto(
        @JsonProperty("id") String id,
        @JsonProperty("keyword") String keyword,
        @JsonProperty("whole_word") boolean wholeWord) {
}
