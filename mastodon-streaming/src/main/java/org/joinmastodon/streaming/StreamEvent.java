package org.joinmastodon.streaming;

public record StreamEvent(String event, String payload) {
}
