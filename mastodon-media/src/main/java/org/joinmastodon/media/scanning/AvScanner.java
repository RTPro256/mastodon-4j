package org.joinmastodon.media.scanning;

import java.nio.file.Path;

public interface AvScanner {
    void scan(Path path) throws AvScannerException;
}
