package org.joinmastodon.web.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Filter;
import org.joinmastodon.core.entity.FilterKeyword;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.entity.Mention;
import org.joinmastodon.core.entity.Notification;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollOption;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.entity.ReportStatus;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.ApplicationDto;
import org.joinmastodon.web.api.dto.FilterDto;
import org.joinmastodon.web.api.dto.FilterKeywordDto;
import org.joinmastodon.web.api.dto.ListDto;
import org.joinmastodon.web.api.dto.MediaAttachmentDto;
import org.joinmastodon.web.api.dto.MentionDto;
import org.joinmastodon.web.api.dto.NotificationDto;
import org.joinmastodon.web.api.dto.PollDto;
import org.joinmastodon.web.api.dto.PollOptionDto;
import org.joinmastodon.web.api.dto.ReportDto;
import org.joinmastodon.web.api.dto.StatusDto;
import org.joinmastodon.web.api.dto.TagDto;

public final class ApiMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private ApiMapper() {
    }

    public static AccountDto toAccountDto(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDto(
                stringId(account.getId()),
                account.getUsername(),
                account.getAcct(),
                account.getDisplayName(),
                account.isLocked(),
                account.isBot(),
                account.getCreatedAt(),
                account.getNote(),
                account.getUrl(),
                account.getAvatarUrl(),
                account.getHeaderUrl(),
                account.getFollowersCount(),
                account.getFollowingCount(),
                account.getStatusesCount(),
                List.of()
        );
    }

    public static StatusDto toStatusDto(Status status) {
        return toStatusDto(status, true);
    }

    private static StatusDto toStatusDto(Status status, boolean includeReblog) {
        if (status == null) {
            return null;
        }
        StatusDto reblog = includeReblog && status.getReblog() != null
                ? toStatusDto(status.getReblog(), false)
                : null;

        return new StatusDto(
                stringId(status.getId()),
                status.getCreatedAt(),
                stringId(status.getInReplyToId()),
                stringId(status.getInReplyToAccountId()),
                status.isSensitive(),
                status.getSpoilerText(),
                status.getVisibility() != null ? status.getVisibility().toApiValue() : null,
                status.getLanguage(),
                status.getUri(),
                status.getUrl(),
                status.getContent(),
                toAccountDto(status.getAccount()),
                mapMediaAttachments(status.getMediaAttachments()),
                mapMentions(status.getMentions()),
                mapTags(status.getTags()),
                toPollDto(status.getPoll()),
                reblog
        );
    }

    private static List<MediaAttachmentDto> mapMediaAttachments(List<MediaAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream().map(ApiMapper::toMediaAttachmentDto).toList();
    }

    public static MediaAttachmentDto toMediaAttachmentDto(MediaAttachment attachment) {
        return new MediaAttachmentDto(
                stringId(attachment.getId()),
                attachment.getType(),
                attachment.getUrl(),
                attachment.getPreviewUrl(),
                attachment.getRemoteUrl(),
                parseMeta(attachment.getMetaJson()),
                attachment.getDescription(),
                attachment.getBlurhash()
        );
    }

    private static List<MentionDto> mapMentions(List<Mention> mentions) {
        if (mentions == null || mentions.isEmpty()) {
            return List.of();
        }
        return mentions.stream().map(ApiMapper::toMentionDto).toList();
    }

    private static MentionDto toMentionDto(Mention mention) {
        Account account = mention.getAccount();
        String id = account != null ? stringId(account.getId()) : stringId(mention.getId());
        String username = firstNonBlank(mention.getUsername(), account != null ? account.getUsername() : null);
        String acct = firstNonBlank(mention.getAcct(), account != null ? account.getAcct() : null);
        String url = firstNonBlank(mention.getUrl(), account != null ? account.getUrl() : null);
        return new MentionDto(id, username, acct, url);
    }

    private static List<TagDto> mapTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream().map(ApiMapper::toTagDto).toList();
    }

    private static TagDto toTagDto(Tag tag) {
        return new TagDto(tag.getName(), tag.getUrl());
    }

    public static PollDto toPollDto(Poll poll) {
        if (poll == null) {
            return null;
        }
        Instant expiresAt = poll.getExpiresAt();
        boolean expired = expiresAt != null && expiresAt.isBefore(Instant.now());
        List<PollOptionDto> options = poll.getOptions() == null ? List.of()
                : poll.getOptions().stream().map(ApiMapper::toPollOptionDto).toList();
        return new PollDto(
                stringId(poll.getId()),
                expiresAt,
                expired,
                poll.isMultiple(),
                poll.getVotesCount(),
                poll.getVotersCount(),
                null,
                options
        );
    }

    private static PollOptionDto toPollOptionDto(PollOption option) {
        return new PollOptionDto(option.getTitle(), option.getVotesCount());
    }

    private static Map<String, Object> parseMeta(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(metaJson, MAP_TYPE);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private static String stringId(Long id) {
        return id == null ? null : Long.toString(id);
    }

    public static NotificationDto toNotificationDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationDto(
                stringId(notification.getId()),
                notification.getType() != null ? notification.getType().toApiValue() : null,
                notification.getCreatedAt(),
                toAccountDto(notification.getActor()),
                toStatusDto(notification.getStatus())
        );
    }

    public static ApplicationDto toApplicationDto(Application application) {
        if (application == null) {
            return null;
        }
        return new ApplicationDto(
                stringId(application.getId()),
                application.getName(),
                application.getWebsite(),
                application.getClientId(),
                application.getClientSecret(),
                application.getRedirectUri()
        );
    }

    public static ListDto toListDto(org.joinmastodon.core.entity.ListEntity list) {
        if (list == null) {
            return null;
        }
        return new ListDto(stringId(list.getId()), list.getTitle());
    }

    public static FilterDto toFilterDto(Filter filter) {
        if (filter == null) {
            return null;
        }
        return new FilterDto(
                stringId(filter.getId()),
                filter.getTitle(),
                splitContext(filter.getContext()),
                filter.getExpiresAt(),
                filter.getFilterAction(),
                mapFilterKeywords(filter.getKeywords())
        );
    }

    private static List<FilterKeywordDto> mapFilterKeywords(List<FilterKeyword> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }
        return keywords.stream().map(ApiMapper::toFilterKeywordDto).toList();
    }

    private static FilterKeywordDto toFilterKeywordDto(FilterKeyword keyword) {
        return new FilterKeywordDto(
                stringId(keyword.getId()),
                keyword.getKeyword(),
                keyword.isWholeWord()
        );
    }

    public static ReportDto toReportDto(Report report) {
        if (report == null) {
            return null;
        }
        List<String> statusIds = report.getStatuses() == null ? List.of()
                : report.getStatuses().stream()
                .map(ReportStatus::getStatus)
                .map(Status::getId)
                .map(ApiMapper::stringId)
                .toList();
        return new ReportDto(
                stringId(report.getId()),
                report.getCreatedAt(),
                toAccountDto(report.getAccount()),
                toAccountDto(report.getTargetAccount()),
                report.getComment(),
                statusIds,
                report.isActionTaken()
        );
    }

    private static List<String> splitContext(String context) {
        if (context == null || context.isBlank()) {
            return List.of();
        }
        return List.of(context.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }
}
