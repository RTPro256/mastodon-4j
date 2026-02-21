package org.joinmastodon.torrent.bencode;

/**
 * Exception thrown when bencode encoding or decoding fails.
 */
public class BencodeException extends RuntimeException {

    public BencodeException(String message) {
        super(message);
    }

    public BencodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BencodeException(Throwable cause) {
        super(cause);
    }
}
