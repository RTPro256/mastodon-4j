package org.joinmastodon.torrent.magnet;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Represents a magnet link.
 * 
 * Magnet links follow the format:
 * magnet:?xt=urn:btih:<infohash>&dn=<name>&tr=<tracker>&xl=<size>
 * 
 * Parameters:
 * - xt: Exact topic (required) - urn:btih: followed by infohash
 * - dn: Display name (optional)
 * - tr: Tracker URL (optional, can be multiple)
 * - xl: Exact length in bytes (optional)
 * - xs: Exact source (optional)
 * - as: Acceptable source (optional)
 * - kt: Keywords (optional)
 */
public class MagnetLink {

    private static final String MAGNET_SCHEME = "magnet";
    private static final String BTIH_PREFIX = "urn:btih:";

    private final String infoHash;
    private String displayName;
    private long exactLength = -1;
    private final List<String> trackers = new ArrayList<>();
    private final List<String> exactSources = new ArrayList<>();
    private final List<String> acceptableSources = new ArrayList<>();
    private final List<String> keywords = new ArrayList<>();

    public MagnetLink(String infoHash) {
        if (!isValidInfoHash(infoHash)) {
            throw new IllegalArgumentException("Invalid infohash: " + infoHash);
        }
        this.infoHash = infoHash.toLowerCase();
    }

    /**
     * Get the infohash.
     */
    public String getInfoHash() {
        return infoHash;
    }

    /**
     * Get the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the display name.
     */
    public MagnetLink displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the exact length.
     */
    public long getExactLength() {
        return exactLength;
    }

    /**
     * Set the exact length.
     */
    public MagnetLink exactLength(long exactLength) {
        this.exactLength = exactLength;
        return this;
    }

    /**
     * Get the tracker URLs.
     */
    public List<String> getTrackers() {
        return new ArrayList<>(trackers);
    }

    /**
     * Add a tracker URL.
     */
    public MagnetLink tracker(String tracker) {
        this.trackers.add(tracker);
        return this;
    }

    /**
     * Add multiple tracker URLs.
     */
    public MagnetLink trackers(List<String> trackers) {
        this.trackers.addAll(trackers);
        return this;
    }

    /**
     * Get the exact sources.
     */
    public List<String> getExactSources() {
        return new ArrayList<>(exactSources);
    }

    /**
     * Add an exact source.
     */
    public MagnetLink exactSource(String source) {
        this.exactSources.add(source);
        return this;
    }

    /**
     * Get the acceptable sources.
     */
    public List<String> getAcceptableSources() {
        return new ArrayList<>(acceptableSources);
    }

    /**
     * Add an acceptable source.
     */
    public MagnetLink acceptableSource(String source) {
        this.acceptableSources.add(source);
        return this;
    }

    /**
     * Get the keywords.
     */
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }

    /**
     * Add a keyword.
     */
    public MagnetLink keyword(String keyword) {
        this.keywords.add(keyword);
        return this;
    }

    /**
     * Add multiple keywords.
     */
    public MagnetLink keywords(List<String> keywords) {
        this.keywords.addAll(keywords);
        return this;
    }

    /**
     * Build the magnet URI string.
     */
    public String toUri() {
        StringBuilder sb = new StringBuilder("magnet:?");
        
        // xt (exact topic) - required
        sb.append("xt=").append(BTIH_PREFIX).append(infoHash);
        
        // dn (display name)
        if (displayName != null && !displayName.isEmpty()) {
            sb.append("&dn=").append(urlEncode(displayName));
        }
        
        // xl (exact length)
        if (exactLength > 0) {
            sb.append("&xl=").append(exactLength);
        }
        
        // tr (trackers)
        for (String tracker : trackers) {
            sb.append("&tr=").append(urlEncode(tracker));
        }
        
        // xs (exact sources)
        for (String source : exactSources) {
            sb.append("&xs=").append(urlEncode(source));
        }
        
        // as (acceptable sources)
        for (String source : acceptableSources) {
            sb.append("&as=").append(urlEncode(source));
        }
        
        // kt (keywords)
        if (!keywords.isEmpty()) {
            sb.append("&kt=").append(urlEncode(String.join(",", keywords)));
        }
        
        return sb.toString();
    }

    /**
     * Parse a magnet URI string.
     *
     * @param magnetUri the magnet URI string
     * @return the parsed MagnetLink
     * @throws IllegalArgumentException if the URI is invalid
     */
    public static MagnetLink fromUri(String magnetUri) {
        if (magnetUri == null || !magnetUri.startsWith("magnet:?")) {
            throw new IllegalArgumentException("Invalid magnet URI: must start with 'magnet:?'");
        }
        
        // Parse query parameters
        String query = magnetUri.substring(8); // Remove "magnet:?"
        Map<String, List<String>> params = parseQuery(query);
        
        // Extract xt (exact topic) - required
        List<String> xtValues = params.get("xt");
        if (xtValues == null || xtValues.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: xt");
        }
        
        String xt = xtValues.get(0);
        if (!xt.startsWith(BTIH_PREFIX)) {
            throw new IllegalArgumentException("Invalid xt parameter: must start with 'urn:btih:'");
        }
        
        String infoHash = xt.substring(BTIH_PREFIX.length());
        MagnetLink magnet = new MagnetLink(infoHash);
        
        // Extract dn (display name)
        List<String> dnValues = params.get("dn");
        if (dnValues != null && !dnValues.isEmpty()) {
            magnet.displayName(urlDecode(dnValues.get(0)));
        }
        
        // Extract xl (exact length)
        List<String> xlValues = params.get("xl");
        if (xlValues != null && !xlValues.isEmpty()) {
            try {
                magnet.exactLength(Long.parseLong(xlValues.get(0)));
            } catch (NumberFormatException e) {
                // Ignore invalid length
            }
        }
        
        // Extract tr (trackers)
        List<String> trValues = params.get("tr");
        if (trValues != null) {
            for (String tracker : trValues) {
                magnet.tracker(urlDecode(tracker));
            }
        }
        
        // Extract xs (exact sources)
        List<String> xsValues = params.get("xs");
        if (xsValues != null) {
            for (String source : xsValues) {
                magnet.exactSource(urlDecode(source));
            }
        }
        
        // Extract as (acceptable sources)
        List<String> asValues = params.get("as");
        if (asValues != null) {
            for (String source : asValues) {
                magnet.acceptableSource(urlDecode(source));
            }
        }
        
        // Extract kt (keywords)
        List<String> ktValues = params.get("kt");
        if (ktValues != null && !ktValues.isEmpty()) {
            String[] kws = urlDecode(ktValues.get(0)).split(",");
            for (String kw : kws) {
                magnet.keyword(kw.trim());
            }
        }
        
        return magnet;
    }

    /**
     * Validate an infohash.
     */
    public static boolean isValidInfoHash(String infoHash) {
        if (infoHash == null) {
            return false;
        }
        
        // SHA-1 infohash is 40 hex characters
        // SHA-256 infohash (BEP 46) is 64 hex characters
        // Base32 encoded infohash is 32 characters
        int len = infoHash.length();
        if (len != 40 && len != 64 && len != 32) {
            return false;
        }
        
        for (char c : infoHash.toCharArray()) {
            if (!Character.isDigit(c) && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                // Check for base32 characters
                if (len == 32 && ((c >= 'A' && c <= 'Z') || (c >= '2' && c <= '7'))) {
                    continue;
                }
                return false;
            }
        }
        
        return true;
    }

    /**
     * Parse query parameters from a magnet URI.
     */
    private static Map<String, List<String>> parseQuery(String query) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex > 0) {
                String key = pair.substring(0, eqIndex);
                String value = pair.substring(eqIndex + 1);
                params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        
        return params;
    }

    /**
     * URL encode a string.
     */
    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * URL decode a string.
     */
    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return toUri();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagnetLink that = (MagnetLink) o;
        return infoHash.equals(that.infoHash);
    }

    @Override
    public int hashCode() {
        return infoHash.hashCode();
    }
}
