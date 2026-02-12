package org.joinmastodon.web.streaming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.service.FollowService;
import org.joinmastodon.web.api.ApiMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StreamingNotifier {
    private static final String CHANNEL_PUBLIC = "public";
    private static final String CHANNEL_PUBLIC_LOCAL = "public_local";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final FollowService followService;

    public StreamingNotifier(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, FollowService followService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.followService = followService;
    }

    public void notifyStatus(Status status) {
        StreamEvent event = new StreamEvent("update", toJson(ApiMapper.toStatusDto(status)));
        Visibility visibility = status.getVisibility();
        if (visibility == Visibility.PUBLIC || visibility == Visibility.UNLISTED) {
            notifyChannel(CHANNEL_PUBLIC, event);
            notifyChannel(CHANNEL_PUBLIC_LOCAL, event);
        }
        notifyUser(status.getAccount().getId(), event);
        List<Follow> followers = followService.findByTargetAccount(status.getAccount());
        for (Follow follow : followers) {
            notifyUser(follow.getAccount().getId(), event);
        }
        if (status.getTags() != null) {
            for (Tag tag : status.getTags()) {
                if (tag != null && tag.getName() != null) {
                    notifyChannel("tag_" + sanitize(tag.getName()), event);
                }
            }
        }
    }

    private void notifyUser(Long accountId, StreamEvent event) {
        if (accountId == null) {
            return;
        }
        notifyChannel("user_" + accountId, event);
    }

    private void notifyChannel(String channel, StreamEvent event) {
        if (channel == null || channel.isBlank()) {
            return;
        }
        String payload = toJson(event);
        jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
            try (var statement = connection.prepareStatement("select pg_notify(?, ?)")) {
                statement.setString(1, channel);
                statement.setString(2, payload);
                statement.execute();
            }
            return null;
        });
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
