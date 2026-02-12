package org.joinmastodon.media.scanning;

public class AvScannerException extends Exception {
    public AvScannerException(String message) {
        super(message);
    }

    public AvScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
