package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListCreateRequest(
        @JsonProperty("title")
        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title) {
}
