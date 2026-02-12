package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchResultsDto(
        @JsonProperty("accounts") List<AccountDto> accounts,
        @JsonProperty("statuses") List<StatusDto> statuses,
        @JsonProperty("hashtags") List<TagDto> hashtags) {
}
