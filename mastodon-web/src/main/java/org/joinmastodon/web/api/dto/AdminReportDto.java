package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record AdminReportDto(
        @JsonProperty("id") String id,
        @JsonProperty("action_taken") boolean actionTaken,
        @JsonProperty("action_taken_at") Instant actionTakenAt,
        @JsonProperty("category") String category,
        @JsonProperty("comment") String comment,
        @JsonProperty("forwarded") boolean forwarded,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("account") AccountDto account,
        @JsonProperty("target_account") AccountDto targetAccount,
        @JsonProperty("assigned_account") AccountDto assignedAccount,
        @JsonProperty("action_taken_by_account") AccountDto actionTakenByAccount,
        @JsonProperty("statuses") List<StatusDto> statuses,
        @JsonProperty("rules") List<String> rules) {
}
