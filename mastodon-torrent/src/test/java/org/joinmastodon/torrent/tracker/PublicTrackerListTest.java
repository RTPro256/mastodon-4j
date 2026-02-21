package org.joinmastodon.torrent.tracker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PublicTrackerList.
 */
class PublicTrackerListTest {

    @Test
    void getDefaultUdpTrackers() {
        List<String> trackers = PublicTrackerList.getDefaultUdpTrackers();
        
        assertFalse(trackers.isEmpty());
        assertTrue(trackers.stream().allMatch(PublicTrackerList::isUdpTracker));
    }

    @Test
    void getDefaultHttpTrackers() {
        List<String> trackers = PublicTrackerList.getDefaultHttpTrackers();
        
        assertFalse(trackers.isEmpty());
        assertTrue(trackers.stream().allMatch(PublicTrackerList::isHttpTracker));
    }

    @Test
    void getDefaultWebTorrentTrackers() {
        List<String> trackers = PublicTrackerList.getDefaultWebTorrentTrackers();
        
        assertFalse(trackers.isEmpty());
        assertTrue(trackers.stream().allMatch(PublicTrackerList::isWebSocketTracker));
    }

    @Test
    void getRecommendedTrackers() {
        List<String> trackers = PublicTrackerList.getRecommendedTrackers();
        
        assertFalse(trackers.isEmpty());
        // Should have at least one of each type
        assertTrue(trackers.stream().anyMatch(PublicTrackerList::isUdpTracker));
        assertTrue(trackers.stream().anyMatch(PublicTrackerList::isWebSocketTracker));
    }

    @Test
    void getTrackerTiers() {
        List<List<String>> tiers = PublicTrackerList.getTrackerTiers();
        
        assertFalse(tiers.isEmpty());
        // Each tier should have at least one tracker
        assertTrue(tiers.stream().allMatch(tier -> !tier.isEmpty()));
    }

    @Test
    void isUdpTracker() {
        assertTrue(PublicTrackerList.isUdpTracker("udp://tracker.example.com:1337/announce"));
        assertFalse(PublicTrackerList.isUdpTracker("http://tracker.example.com:6969/announce"));
        assertFalse(PublicTrackerList.isUdpTracker(null));
    }

    @Test
    void isHttpTracker() {
        assertTrue(PublicTrackerList.isHttpTracker("http://tracker.example.com:6969/announce"));
        assertTrue(PublicTrackerList.isHttpTracker("https://tracker.example.com:443/announce"));
        assertFalse(PublicTrackerList.isHttpTracker("udp://tracker.example.com:1337/announce"));
        assertFalse(PublicTrackerList.isHttpTracker(null));
    }

    @Test
    void isWebSocketTracker() {
        assertTrue(PublicTrackerList.isWebSocketTracker("wss://tracker.example.com:443/announce"));
        assertTrue(PublicTrackerList.isWebSocketTracker("ws://tracker.example.com:80/announce"));
        assertFalse(PublicTrackerList.isWebSocketTracker("udp://tracker.example.com:1337/announce"));
        assertFalse(PublicTrackerList.isWebSocketTracker(null));
    }

    @Test
    void parseUdpTrackerUrl() {
        PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(
            "udp://tracker.example.com:1337/announce");
        
        assertEquals("udp", url.protocol());
        assertEquals("tracker.example.com", url.host());
        assertEquals(1337, url.port());
        assertEquals("/announce", url.path());
    }

    @Test
    void parseHttpTrackerUrl() {
        PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(
            "http://tracker.example.com:6969/announce");
        
        assertEquals("http", url.protocol());
        assertEquals("tracker.example.com", url.host());
        assertEquals(6969, url.port());
        assertEquals("/announce", url.path());
    }

    @Test
    void parseWebSocketTrackerUrl() {
        PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(
            "wss://tracker.example.com:443/announce");
        
        assertEquals("wss", url.protocol());
        assertEquals("tracker.example.com", url.host());
        assertEquals(443, url.port());
        assertEquals("/announce", url.path());
    }

    @Test
    void parseTrackerUrlWithoutPort() {
        PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(
            "http://tracker.example.com/announce");
        
        assertEquals("http", url.protocol());
        assertEquals("tracker.example.com", url.host());
        assertEquals(80, url.port()); // Default HTTP port
    }

    @Test
    void parseInvalidTrackerUrlThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            PublicTrackerList.parseTrackerUrl("invalid-url"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            PublicTrackerList.parseTrackerUrl(null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            PublicTrackerList.parseTrackerUrl(""));
    }

    @Test
    void trackerUrlGetFullUrl() {
        PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(
            "udp://tracker.example.com:1337/announce");
        
        assertEquals("udp://tracker.example.com:1337/announce", url.getFullUrl());
    }
}
