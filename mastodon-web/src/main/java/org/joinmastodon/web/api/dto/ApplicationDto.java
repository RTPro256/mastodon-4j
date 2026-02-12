package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApplicationDto(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("website") String website,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        @JsonProperty("redirect_uri") String redirectUri,
        @JsonProperty("scopes") String scopes) {
}
