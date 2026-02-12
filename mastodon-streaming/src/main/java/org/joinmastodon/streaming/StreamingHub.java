package org.joinmastodon.streaming;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class StreamingHub {
    private final Map<String, Sinks.Many<ServerSentEvent<String>>> sinks = new ConcurrentHashMap<>();

    public Flux<ServerSentEvent<String>> subscribe(String channel) {
        Sinks.Many<ServerSentEvent<String>> sink = sinks.computeIfAbsent(
                channel,
                key -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux();
    }

    public void publish(String channel, StreamEvent event) {
        if (channel == null || channel.isBlank() || event == null) {
            return;
        }
        Sinks.Many<ServerSentEvent<String>> sink = sinks.get(channel);
        if (sink == null) {
            return;
        }
        ServerSentEvent<String> sse = ServerSentEvent.<String>builder(event.payload())
                .event(event.event())
                .build();
        sink.tryEmitNext(sse);
    }
}
