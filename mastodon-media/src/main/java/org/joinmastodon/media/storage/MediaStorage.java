package org.joinmastodon.media.storage;

import java.io.IOException;
import java.nio.file.Path;

public interface MediaStorage {
    String save(String key, byte[] data) throws IOException;

    byte[] load(String key) throws IOException;

    boolean delete(String key) throws IOException;

    String resolveUrl(String key);

    default Path resolvePath(String key) {
        throw new UnsupportedOperationException("Storage does not expose file paths");
    }
}
