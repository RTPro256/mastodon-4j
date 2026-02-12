package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record AccountDto(
        @JsonProperty("id") String id,
        @JsonProperty("username") String username,
        @JsonProperty("acct") String acct,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("locked") boolean locked,
        @JsonProperty("bot") boolean bot,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("note") String note,
        @JsonProperty("url") String url,
        @JsonProperty("avatar") String avatar,
        @JsonProperty("header") String header,
        @JsonProperty("followers_count") int followersCount,
        @JsonProperty("following_count") int followingCount,
        @JsonProperty("statuses_count") int statusesCount,
        @JsonProperty("fields") List<ProfileFieldDto> fields) {
}
