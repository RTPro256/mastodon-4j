package org.joinmastodon.torrent.tracker;

import org.joinmastodon.torrent.bencode.BencodeDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP tracker client implementing the standard BitTorrent HTTP tracker protocol.
 * 
 * HTTP tracker announce URL format:
 * http://tracker.example.com:6969/announce?info_hash=<hash>&peer_id=<id>&port=<port>&...
 * 
 * Response is a bencoded dictionary containing:
 * - interval: seconds between announces
 * - peers: list of peer info (compact or dictionary format)
 * - complete: number of seeders
 * - incomplete: number of leechers
 */
public class HttpTrackerClient extends TrackerClient {

    private static final Logger log = LoggerFactory.getLogger(HttpTrackerClient.class);

    private final BencodeDecoder decoder;

    public HttpTrackerClient() {
        super();
        this.decoder = new BencodeDecoder();
    }

    public HttpTrackerClient(int timeoutMs, int retryCount) {
        super(timeoutMs, retryCount);
        this.decoder = new BencodeDecoder();
    }

    @Override
    public CompletableFuture<TrackerAnnounceResult> announce(
            String trackerUrl,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,
            AnnounceEvent event) {

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Build announce URL
                String announceUrl = buildAnnounceUrl(trackerUrl, infoHash, peerId, port,
                        uploaded, downloaded, left, event);
                log.debug("Announcing to HTTP tracker: {}", announceUrl);

                // Make HTTP request
                HttpURLConnection connection = (HttpURLConnection) new URL(announceUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeoutMs);
                connection.setReadTimeout(timeoutMs);
                connection.setRequestProperty("User-Agent", "mastodon-torrent/1.0");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    String errorMsg = "HTTP error: " + responseCode;
                    if (connection.getErrorStream() != null) {
                        errorMsg += " - " + readStream(connection.getErrorStream());
                    }
                    return TrackerAnnounceResult.failure(trackerUrl, errorMsg,
                            System.currentTimeMillis() - startTime);
                }

                // Read and parse response
                byte[] responseData = readAllBytes(connection.getInputStream());
                Map<String, Object> response = parseResponse(responseData);

                // Check for failure reason
                if (response.containsKey("failure reason")) {
                    String failureReason = new String((byte[]) response.get("failure reason"), StandardCharsets.UTF_8);
                    return TrackerAnnounceResult.failure(trackerUrl, failureReason,
                            System.currentTimeMillis() - startTime);
                }

                // Parse response
                int interval = getInt(response, "interval", 1800);
                int minInterval = getInt(response, "min interval", 300);
                int seeders = getInt(response, "complete", 0);
                int leechers = getInt(response, "incomplete", 0);

                // Parse peers
                List<TrackerAnnounceResult.PeerInfo> peers = parsePeers(response);

                long elapsed = System.currentTimeMillis() - startTime;
                log.debug("HTTP tracker announce successful: {} peers, {} seeders, {} leechers",
                        peers.size(), seeders, leechers);

                return TrackerAnnounceResult.success(trackerUrl, seeders, leechers, interval, peers, elapsed);

            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - startTime;
                log.warn("Failed to announce to HTTP tracker {}: {}", trackerUrl, e.getMessage());
                return TrackerAnnounceResult.failure(trackerUrl, e.getMessage(), elapsed);
            }
        });
    }

    @Override
    public CompletableFuture<ScrapeResult> scrape(String trackerUrl, byte[]... infoHashes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build scrape URL
                String scrapeUrl = buildScrapeUrl(trackerUrl, infoHashes);
                log.debug("Scraping HTTP tracker: {}", scrapeUrl);

                // Make HTTP request
                HttpURLConnection connection = (HttpURLConnection) new URL(scrapeUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeoutMs);
                connection.setReadTimeout(timeoutMs);
                connection.setRequestProperty("User-Agent", "mastodon-torrent/1.0");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return new ScrapeResult(trackerUrl, ScrapeResult.Status.FAILED,
                            "HTTP error: " + responseCode, List.of());
                }

                // Read and parse response
                byte[] responseData = readAllBytes(connection.getInputStream());
                Map<String, Object> response = parseResponse(responseData);

                // Parse scrape entries
                List<ScrapeEntry> entries = new ArrayList<>();
                @SuppressWarnings("unchecked")
                Map<byte[], Object> files = (Map<byte[], Object>) response.get("files");
                
                if (files != null) {
                    for (Map.Entry<byte[], Object> entry : files.entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fileData = (Map<String, Object>) entry.getValue();
                        
                        int seeders = getInt(fileData, "complete", 0);
                        int completed = getInt(fileData, "downloaded", 0);
                        int leechers = getInt(fileData, "incomplete", 0);
                        
                        entries.add(new ScrapeEntry(entry.getKey(), seeders, completed, leechers));
                    }
                }

                return new ScrapeResult(trackerUrl, ScrapeResult.Status.SUCCESS, null, entries);

            } catch (Exception e) {
                log.warn("Failed to scrape HTTP tracker {}: {}", trackerUrl, e.getMessage());
                return new ScrapeResult(trackerUrl, ScrapeResult.Status.FAILED, e.getMessage(), List.of());
            }
        });
    }

    @Override
    public boolean supports(String trackerUrl) {
        return PublicTrackerList.isHttpTracker(trackerUrl);
    }

    /**
     * Build the announce URL with query parameters.
     */
    private String buildAnnounceUrl(
            String trackerUrl,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,
            AnnounceEvent event) {

        StringBuilder url = new StringBuilder(trackerUrl);
        url.append("?info_hash=").append(urlEncode(infoHash));
        url.append("&peer_id=").append(urlEncode(peerId));
        url.append("&port=").append(port);
        url.append("&uploaded=").append(uploaded);
        url.append("&downloaded=").append(downloaded);
        url.append("&left=").append(left);
        url.append("&compact=1"); // Request compact peer format
        url.append("&numwant=50"); // Number of peers to request

        if (event != AnnounceEvent.NONE) {
            url.append("&event=").append(event.name().toLowerCase());
        }

        return url.toString();
    }

    /**
     * Build the scrape URL.
     */
    private String buildScrapeUrl(String trackerUrl, byte[]... infoHashes) {
        // Replace "announce" with "scrape" in the URL
        String scrapeUrl = trackerUrl.replace("/announce", "/scrape");

        StringBuilder url = new StringBuilder(scrapeUrl);
        url.append("?");

        for (int i = 0; i < infoHashes.length; i++) {
            if (i > 0) {
                url.append("&");
            }
            url.append("info_hash=").append(urlEncode(infoHashes[i]));
        }

        return url.toString();
    }

    /**
     * Parse the bencoded response.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(byte[] data) {
        Object decoded = decoder.decodeWithStringKeys(data);
        if (!(decoded instanceof Map)) {
            throw new RuntimeException("Invalid tracker response: expected dictionary");
        }
        return (Map<String, Object>) decoded;
    }

    /**
     * Parse peers from the response.
     */
    @SuppressWarnings("unchecked")
    private List<TrackerAnnounceResult.PeerInfo> parsePeers(Map<String, Object> response) {
        List<TrackerAnnounceResult.PeerInfo> peers = new ArrayList<>();

        Object peersObj = response.get("peers");
        if (peersObj == null) {
            return peers;
        }

        if (peersObj instanceof byte[]) {
            // Compact format: 6 bytes per peer (4 IP + 2 port)
            byte[] compactPeers = (byte[]) peersObj;
            ByteBuffer buffer = ByteBuffer.wrap(compactPeers);

            while (buffer.remaining() >= 6) {
                String ip = String.format("%d.%d.%d.%d",
                        buffer.get() & 0xFF,
                        buffer.get() & 0xFF,
                        buffer.get() & 0xFF,
                        buffer.get() & 0xFF);
                int port = buffer.getShort() & 0xFFFF;
                peers.add(new TrackerAnnounceResult.PeerInfo(ip, port));
            }
        } else if (peersObj instanceof List) {
            // Dictionary format
            List<Map<String, Object>> peerList = (List<Map<String, Object>>) peersObj;
            for (Map<String, Object> peerData : peerList) {
                String ip = getString(peerData, "ip");
                int port = getInt(peerData, "port", 0);
                byte[] peerId = getBytes(peerData, "peer id");
                peers.add(new TrackerAnnounceResult.PeerInfo(ip, port, peerId));
            }
        }

        return peers;
    }

    /**
     * URL-encode a byte array.
     */
    private String urlEncode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%%%02x", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Read all bytes from an input stream.
     */
    private byte[] readAllBytes(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        return out.toByteArray();
    }

    /**
     * Read a string from an input stream.
     */
    private String readStream(InputStream in) throws IOException {
        return new String(readAllBytes(in), StandardCharsets.UTF_8);
    }

    /**
     * Get an integer value from a map.
     */
    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    /**
     * Get a string value from a map.
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value.toString();
    }

    /**
     * Get a byte array value from a map.
     */
    private byte[] getBytes(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }
}
