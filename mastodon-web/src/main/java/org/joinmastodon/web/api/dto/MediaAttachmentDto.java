package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record MediaAttachmentDto(
        @JsonProperty("id") String id,
        @JsonProperty("type") String type,
        @JsonProperty("url") String url,
        @JsonProperty("preview_url") String previewUrl,
        @JsonProperty("remote_url") String remoteUrl,
        @JsonProperty("meta") Map<String, Object> meta,
        @JsonProperty("description") String description,
        @JsonProperty("blurhash") String blurhash) {
}
