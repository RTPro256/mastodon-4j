package org.joinmastodon.streaming.api;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/streaming")
public class StreamingController {

    @GetMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> userStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "access_token", required = false) String accessToken) {
        requireToken(authorization, accessToken);
        return heartbeatStream("user");
    }

    @GetMapping(value = "/public", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> publicStream() {
        return heartbeatStream("public");
    }

    @GetMapping(value = "/public/local", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> publicLocalStream() {
        return heartbeatStream("public:local");
    }

    @GetMapping(value = "/hashtag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> hashtagStream(
            @RequestParam(value = "tag", required = false) String tag) {
        return heartbeatStream(tag == null ? "hashtag" : "hashtag:" + tag);
    }

    @GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> listStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "list", required = false) String listId) {
        requireToken(authorization, accessToken);
        return heartbeatStream(listId == null ? "list" : "list:" + listId);
    }

    private void requireToken(String authorization, String accessToken) {
        boolean hasBearer = authorization != null && authorization.startsWith("Bearer ");
        boolean hasQueryToken = accessToken != null && !accessToken.isBlank();
        if (!hasBearer && !hasQueryToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing access token");
        }
    }

    private Flux<ServerSentEvent<String>> heartbeatStream(String stream) {
        return Flux.interval(Duration.ofSeconds(15))
                .map(seq -> ServerSentEvent.<String>builder()
                        .event("heartbeat")
                        .data(stream)
                        .build());
    }
}
