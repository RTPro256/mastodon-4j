package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record FilterCreateRequest(
        @JsonProperty("title")
        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title,

        @JsonProperty("context")
        @NotEmpty(message = "context is required")
        List<String> context,

        @JsonProperty("filter_action")
        @NotBlank(message = "filter_action is required")
        @Size(max = 32)
        String filterAction,

        @JsonProperty("expires_at")
        Instant expiresAt,

        @JsonProperty("keywords")
        @Valid
        List<FilterKeywordRequest> keywords) {
}
