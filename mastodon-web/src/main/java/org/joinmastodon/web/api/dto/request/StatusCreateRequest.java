package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record StatusCreateRequest(
        @JsonProperty("status")
        @NotBlank(message = "status is required")
        String status,

        @JsonProperty("visibility")
        @Size(max = 16)
        String visibility,

        @JsonProperty("sensitive")
        Boolean sensitive,

        @JsonProperty("spoiler_text")
        @Size(max = 500)
        String spoilerText,

        @JsonProperty("language")
        @Size(max = 10)
        String language,

        @JsonProperty("in_reply_to_id")
        String inReplyToId,

        @JsonProperty("media_ids")
        List<String> mediaIds) {
}
