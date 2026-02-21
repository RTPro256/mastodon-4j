package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Admin-specific account DTO with additional fields like email.
 * Used by /api/v1/admin/accounts endpoints.
 */
public record AdminAccountDto(
        @JsonProperty("id") String id,
        @JsonProperty("username") String username,
        @JsonProperty("domain") String domain,
        @JsonProperty("email") String email,
        @JsonProperty("ip") String ip,
        @JsonProperty("locale") String locale,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("confirmed") boolean confirmed,
        @JsonProperty("suspended") boolean suspended,
        @JsonProperty("silenced") boolean silenced,
        @JsonProperty("disabled") boolean disabled,
        @JsonProperty("approved") boolean approved,
        @JsonProperty("account") AccountDto account) {
}
