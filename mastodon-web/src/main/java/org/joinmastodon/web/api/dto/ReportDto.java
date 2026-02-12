package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ReportDto(
        @JsonProperty("id") String id,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("account") AccountDto account,
        @JsonProperty("target_account") AccountDto targetAccount,
        @JsonProperty("comment") String comment,
        @JsonProperty("status_ids") List<String> statusIds,
        @JsonProperty("action_taken") boolean actionTaken) {
}
