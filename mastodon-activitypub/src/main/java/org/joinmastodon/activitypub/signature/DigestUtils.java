package org.joinmastodon.activitypub.signature;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for computing HTTP message digests.
 * Used for ActivityPub HTTP signature verification with Digest header.
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3230">RFC 3230 - Instance Digests</a>
 */
public final class DigestUtils {

    private DigestUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Compute SHA-256 digest of the given content.
     *
     * @param content the content to digest
     * @return the base64-encoded digest value
     */
    public static String sha256Digest(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Compute SHA-256 digest of the given string content.
     *
     * @param content the content to digest (UTF-8 encoded)
     * @return the base64-encoded digest value
     */
    public static String sha256Digest(String content) {
        return sha256Digest(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Build a Digest header value for SHA-256.
     * Format: SHA-256=<base64-encoded-hash>
     *
     * @param content the content to digest
     * @return the Digest header value
     */
    public static String buildDigestHeader(byte[] content) {
        return "SHA-256=" + sha256Digest(content);
    }

    /**
     * Build a Digest header value for SHA-256 from string content.
     *
     * @param content the content to digest (UTF-8 encoded)
     * @return the Digest header value
     */
    public static String buildDigestHeader(String content) {
        return buildDigestHeader(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verify that the given digest header matches the content.
     *
     * @param digestHeader the Digest header value (format: SHA-256=<hash>)
     * @param content the content to verify
     * @return true if the digest matches
     */
    public static boolean verifyDigest(String digestHeader, byte[] content) {
        if (digestHeader == null || digestHeader.isEmpty()) {
            return false;
        }

        // Parse the digest header
        String[] parts = digestHeader.split("=", 2);
        if (parts.length != 2) {
            return false;
        }

        String algorithm = parts[0].trim();
        String expectedHash = parts[1].trim();

        if ("SHA-256".equalsIgnoreCase(algorithm)) {
            String actualHash = sha256Digest(content);
            return expectedHash.equals(actualHash);
        }

        // Unsupported algorithm
        return false;
    }

    /**
     * Verify that the given digest header matches the string content.
     *
     * @param digestHeader the Digest header value
     * @param content the content to verify (UTF-8 encoded)
     * @return true if the digest matches
     */
    public static boolean verifyDigest(String digestHeader, String content) {
        return verifyDigest(digestHeader, content.getBytes(StandardCharsets.UTF_8));
    }
}