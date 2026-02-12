package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record ListUpdateRequest(
        @JsonProperty("title")
        @Size(max = 255)
        String title) {
}
