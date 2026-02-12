package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RelationshipDto(
        @JsonProperty("id") String id,
        @JsonProperty("following") boolean following,
        @JsonProperty("followed_by") boolean followedBy,
        @JsonProperty("blocking") boolean blocking,
        @JsonProperty("muting") boolean muting) {
}
