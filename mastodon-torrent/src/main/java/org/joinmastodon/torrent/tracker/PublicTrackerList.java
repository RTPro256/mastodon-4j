package org.joinmastodon.torrent.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Built-in list of public BitTorrent trackers.
 * 
 * These trackers are commonly used for public torrent distribution
 * and can be used as defaults when creating new torrents.
 */
public final class PublicTrackerList {

    private PublicTrackerList() {
        // Utility class
    }

    /**
     * Get the default list of public UDP trackers.
     */
    public static List<String> getDefaultUdpTrackers() {
        List<String> trackers = new ArrayList<>();
        
        // UDP trackers (most efficient)
        trackers.add("udp://tracker.opentrackr.org:1337/announce");
        trackers.add("udp://open.stealth.si:80/announce");
        trackers.add("udp://tracker.torrent.eu.org:451/announce");
        trackers.add("udp://tracker.bittor.pw:1337/announce");
        trackers.add("udp://public.popcorn-tracker.org:6969/announce");
        trackers.add("udp://tracker.dler.org:6969/announce");
        trackers.add("udp://exodus.desync.com:6969/announce");
        trackers.add("udp://open.demonii.com:1337/announce");
        trackers.add("udp://tracker.openbittorrent.com:6969/announce");
        trackers.add("udp://tracker.moeking.me:6969/announce");
        
        return Collections.unmodifiableList(trackers);
    }

    /**
     * Get the default list of public HTTP trackers.
     */
    public static List<String> getDefaultHttpTrackers() {
        List<String> trackers = new ArrayList<>();
        
        // HTTP trackers
        trackers.add("http://tracker.opentrackr.org:1337/announce");
        trackers.add("http://tracker.openbittorrent.com:80/announce");
        trackers.add("http://tracker.bt4g.com:2095/announce");
        
        return Collections.unmodifiableList(trackers);
    }

    /**
     * Get the default list of WebTorrent trackers (WebSocket).
     * These enable browser-based peer connections.
     */
    public static List<String> getDefaultWebTorrentTrackers() {
        List<String> trackers = new ArrayList<>();
        
        // WebSocket trackers for WebTorrent
        trackers.add("wss://tracker.openwebtorrent.com:443/announce");
        trackers.add("wss://tracker.btorrent.xyz:443/announce");
        trackers.add("wss://tracker.fastcast.nz:443/announce");
        
        return Collections.unmodifiableList(trackers);
    }

    /**
     * Get all default trackers combined.
     */
    public static List<String> getAllDefaultTrackers() {
        List<String> trackers = new ArrayList<>();
        trackers.addAll(getDefaultUdpTrackers());
        trackers.addAll(getDefaultHttpTrackers());
        trackers.addAll(getDefaultWebTorrentTrackers());
        return Collections.unmodifiableList(trackers);
    }

    /**
     * Get a recommended subset of trackers for new torrents.
     * This provides a balance between redundancy and overhead.
     */
    public static List<String> getRecommendedTrackers() {
        List<String> trackers = new ArrayList<>();
        
        // Best UDP trackers
        trackers.add("udp://tracker.opentrackr.org:1337/announce");
        trackers.add("udp://open.stealth.si:80/announce");
        trackers.add("udp://tracker.torrent.eu.org:451/announce");
        
        // HTTP fallback
        trackers.add("http://tracker.opentrackr.org:1337/announce");
        
        // WebTorrent for browser peers
        trackers.add("wss://tracker.openwebtorrent.com:443/announce");
        
        return Collections.unmodifiableList(trackers);
    }

    /**
     * Get trackers organized in tiers.
     * Each tier is a list of trackers that can be tried in parallel.
     */
    public static List<List<String>> getTrackerTiers() {
        List<List<String>> tiers = new ArrayList<>();
        
        // Tier 1: Primary UDP trackers
        List<String> tier1 = new ArrayList<>();
        tier1.add("udp://tracker.opentrackr.org:1337/announce");
        tier1.add("udp://open.stealth.si:80/announce");
        tiers.add(tier1);
        
        // Tier 2: Secondary UDP trackers
        List<String> tier2 = new ArrayList<>();
        tier2.add("udp://tracker.torrent.eu.org:451/announce");
        tier2.add("udp://tracker.bittor.pw:1337/announce");
        tiers.add(tier2);
        
        // Tier 3: HTTP trackers
        List<String> tier3 = new ArrayList<>();
        tier3.add("http://tracker.opentrackr.org:1337/announce");
        tier3.add("http://tracker.openbittorrent.com:80/announce");
        tiers.add(tier3);
        
        // Tier 4: WebTorrent trackers
        List<String> tier4 = new ArrayList<>();
        tier4.add("wss://tracker.openwebtorrent.com:443/announce");
        tier4.add("wss://tracker.btorrent.xyz:443/announce");
        tiers.add(tier4);
        
        return Collections.unmodifiableList(tiers);
    }

    /**
     * Check if a tracker URL is a UDP tracker.
     */
    public static boolean isUdpTracker(String trackerUrl) {
        return trackerUrl != null && trackerUrl.toLowerCase().startsWith("udp://");
    }

    /**
     * Check if a tracker URL is an HTTP tracker.
     */
    public static boolean isHttpTracker(String trackerUrl) {
        if (trackerUrl == null) {
            return false;
        }
        String lower = trackerUrl.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * Check if a tracker URL is a WebSocket tracker.
     */
    public static boolean isWebSocketTracker(String trackerUrl) {
        if (trackerUrl == null) {
            return false;
        }
        String lower = trackerUrl.toLowerCase();
        return lower.startsWith("ws://") || lower.startsWith("wss://");
    }

    /**
     * Parse tracker URL to extract protocol, host, and port.
     */
    public static TrackerUrl parseTrackerUrl(String trackerUrl) {
        if (trackerUrl == null || trackerUrl.isEmpty()) {
            throw new IllegalArgumentException("Tracker URL cannot be null or empty");
        }
        
        String url = trackerUrl.toLowerCase();
        
        String protocol;
        String remaining;
        
        if (url.startsWith("udp://")) {
            protocol = "udp";
            remaining = url.substring(6);
        } else if (url.startsWith("http://")) {
            protocol = "http";
            remaining = url.substring(7);
        } else if (url.startsWith("https://")) {
            protocol = "https";
            remaining = url.substring(8);
        } else if (url.startsWith("ws://")) {
            protocol = "ws";
            remaining = url.substring(5);
        } else if (url.startsWith("wss://")) {
            protocol = "wss";
            remaining = url.substring(6);
        } else {
            throw new IllegalArgumentException("Unknown tracker protocol: " + trackerUrl);
        }
        
        // Split host:port/path
        int slashIndex = remaining.indexOf('/');
        String hostPort;
        String path = "";
        
        if (slashIndex > 0) {
            hostPort = remaining.substring(0, slashIndex);
            path = remaining.substring(slashIndex);
        } else {
            hostPort = remaining;
        }
        
        // Split host and port
        String host;
        int port;
        
        int colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex > 0) {
            host = hostPort.substring(0, colonIndex);
            port = Integer.parseInt(hostPort.substring(colonIndex + 1));
        } else {
            host = hostPort;
            // Default ports
            port = switch (protocol) {
                case "http" -> 80;
                case "https" -> 443;
                case "ws" -> 80;
                case "wss" -> 443;
                case "udp" -> 6969; // Default UDP tracker port
                default -> 6969;
            };
        }
        
        return new TrackerUrl(protocol, host, port, path);
    }

    /**
     * Parsed tracker URL components.
     */
    public record TrackerUrl(String protocol, String host, int port, String path) {
        public String getHostPort() {
            return host + ":" + port;
        }
        
        public String getFullUrl() {
            return protocol + "://" + host + ":" + port + path;
        }
    }
}
