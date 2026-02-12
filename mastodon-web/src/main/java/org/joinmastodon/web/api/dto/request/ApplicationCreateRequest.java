package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequest(
        @JsonProperty("client_name")
        @NotBlank(message = "client_name is required")
        @Size(max = 255)
        String clientName,

        @JsonProperty("redirect_uris")
        @NotBlank(message = "redirect_uris is required")
        String redirectUris,

        @JsonProperty("scopes")
        @Size(max = 255)
        String scopes,

        @JsonProperty("website")
        @Size(max = 2048)
        String website) {
}
