package org.joinmastodon.web.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record StatusDto(
        @JsonProperty("id") String id,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("in_reply_to_id") String inReplyToId,
        @JsonProperty("in_reply_to_account_id") String inReplyToAccountId,
        @JsonProperty("sensitive") boolean sensitive,
        @JsonProperty("spoiler_text") String spoilerText,
        @JsonProperty("visibility") String visibility,
        @JsonProperty("language") String language,
        @JsonProperty("uri") String uri,
        @JsonProperty("url") String url,
        @JsonProperty("content") String content,
        @JsonProperty("account") AccountDto account,
        @JsonProperty("media_attachments") List<MediaAttachmentDto> mediaAttachments,
        @JsonProperty("mentions") List<MentionDto> mentions,
        @JsonProperty("tags") List<TagDto> tags,
        @JsonProperty("poll") PollDto poll,
        @JsonProperty("reblog") StatusDto reblog,
        @JsonProperty("favourites_count") int favouritesCount,
        @JsonProperty("reblogs_count") int reblogsCount,
        @JsonProperty("replies_count") int repliesCount,
        @JsonProperty("favourited") boolean favourited,
        @JsonProperty("reblogged") boolean reblogged,
        @JsonProperty("bookmarked") boolean bookmarked,
        @JsonProperty("pinned") boolean pinned) {
}
