package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PollVoteRequest(
        @JsonProperty("choices")
        @NotEmpty(message = "choices is required")
        List<Integer> choices) {
}
