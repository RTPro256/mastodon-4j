package org.joinmastodon.media.scanning;

import java.nio.file.Path;

public class NoOpAvScanner implements AvScanner {
    @Override
    public void scan(Path path) throws AvScannerException {
        if (path == null) {
            throw new AvScannerException("Path is required for scanning");
        }
    }
}
