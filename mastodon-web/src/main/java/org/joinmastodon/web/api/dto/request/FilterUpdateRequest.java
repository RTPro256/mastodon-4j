package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record FilterUpdateRequest(
        @JsonProperty("title")
        @Size(max = 255)
        String title,

        @JsonProperty("context")
        List<String> context,

        @JsonProperty("filter_action")
        @Size(max = 32)
        String filterAction,

        @JsonProperty("expires_at")
        Instant expiresAt,

        @JsonProperty("keywords")
        @Valid
        List<FilterKeywordRequest> keywords) {
}
