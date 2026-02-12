package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record FilterKeywordRequest(
        @JsonProperty("keyword")
        @NotBlank(message = "keyword is required")
        String keyword,

        @JsonProperty("whole_word")
        boolean wholeWord) {
}
