package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record NotificationDto(
        @JsonProperty("id") String id,
        @JsonProperty("type") String type,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("account") AccountDto account,
        @JsonProperty("status") StatusDto status) {
}
