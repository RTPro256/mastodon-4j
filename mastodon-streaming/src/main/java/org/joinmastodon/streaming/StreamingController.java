package org.joinmastodon.streaming;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/streaming")
public class StreamingController {
    private final StreamingHub streamingHub;
    private final PostgresNotificationListener notificationListener;
    private final StreamingAuthService authService;

    public StreamingController(StreamingHub streamingHub,
                               PostgresNotificationListener notificationListener,
                               StreamingAuthService authService) {
        this.streamingHub = streamingHub;
        this.notificationListener = notificationListener;
        this.authService = authService;
    }

    @GetMapping(value = "/public", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> publicStream() {
        String channel = "public";
        notificationListener.ensureListening(channel);
        return streamingHub.subscribe(channel);
    }

    @GetMapping(value = "/public/local", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> localPublicStream() {
        String channel = "public_local";
        notificationListener.ensureListening(channel);
        return streamingHub.subscribe(channel);
    }

    @GetMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> userStream(@RequestHeader("Authorization") String authorization) {
        Long accountId = authService.requireAccountId(authorization);
        String channel = "user_" + accountId;
        notificationListener.ensureListening(channel);
        return streamingHub.subscribe(channel);
    }

    @GetMapping(value = "/hashtag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> hashtagStream(
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "hashtag", required = false) String hashtag) {
        String resolved = tag != null ? tag : hashtag;
        String channel = "tag_" + sanitize(resolved);
        notificationListener.ensureListening(channel);
        return streamingHub.subscribe(channel);
    }

    @GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> listStream(
            @RequestParam(value = "list", required = false) String list,
            @RequestParam(value = "list_id", required = false) String listId) {
        String resolved = list != null ? list : listId;
        String channel = "list_" + sanitize(resolved);
        notificationListener.ensureListening(channel);
        return streamingHub.subscribe(channel);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
