package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StatusContextDto(
        @JsonProperty("ancestors") List<StatusDto> ancestors,
        @JsonProperty("descendants") List<StatusDto> descendants) {
}
