package org.joinmastodon.web.api.dto;

import java.util.List;

/**
 * Represents a conversation in Mastodon API.
 */
public record ConversationDto(
    String id,
    List<AccountDto> accounts,
    boolean unread,
    StatusDto last_status
) {}
