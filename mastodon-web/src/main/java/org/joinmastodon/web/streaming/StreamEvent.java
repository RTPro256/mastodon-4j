package org.joinmastodon.web.streaming;

public record StreamEvent(String event, String payload) {
}
