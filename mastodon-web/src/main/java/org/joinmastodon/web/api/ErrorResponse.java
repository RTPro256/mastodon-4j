package org.joinmastodon.web.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription,
        @JsonProperty("details") Map<String, String> details) {

    public static ErrorResponse of(String error, String description) {
        return new ErrorResponse(error, description, null);
    }

    public static ErrorResponse of(String error, String description, Map<String, String> details) {
        return new ErrorResponse(error, description, details);
    }
}
