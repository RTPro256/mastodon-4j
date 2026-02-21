package org.joinmastodon.torrent.authority;

/**
 * Exception thrown when content authority operations fail.
 */
public class ContentAuthorityException extends RuntimeException {

    public ContentAuthorityException(String message) {
        super(message);
    }

    public ContentAuthorityException(String message, Throwable cause) {
        super(message, cause);
    }
}
