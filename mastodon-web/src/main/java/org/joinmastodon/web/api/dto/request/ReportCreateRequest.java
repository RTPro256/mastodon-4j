package org.joinmastodon.web.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReportCreateRequest(
        @JsonProperty("account_id")
        @NotBlank(message = "account_id is required")
        String accountId,

        @JsonProperty("status_ids")
        @NotEmpty(message = "status_ids is required")
        List<String> statusIds,

        @JsonProperty("comment")
        @Size(max = 1000)
        String comment) {
}
