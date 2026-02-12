package org.joinmastodon.media.storage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileSystemMediaStorage implements MediaStorage {
    private final Path basePath;
    private final URI baseUri;

    public LocalFileSystemMediaStorage(String basePath, String baseUrl) {
        this.basePath = Paths.get(basePath).toAbsolutePath();
        this.baseUri = baseUrl == null || baseUrl.isBlank() ? null : URI.create(baseUrl);
    }

    @Override
    public String save(String key, byte[] data) throws IOException {
        Path target = resolvePath(key);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return key;
    }

    @Override
    public byte[] load(String key) throws IOException {
        return Files.readAllBytes(resolvePath(key));
    }

    @Override
    public boolean delete(String key) throws IOException {
        return Files.deleteIfExists(resolvePath(key));
    }

    @Override
    public String resolveUrl(String key) {
        if (baseUri == null) {
            return resolvePath(key).toUri().toString();
        }
        return baseUri.resolve(key).toString();
    }

    private Path resolvePath(String key) {
        return basePath.resolve(key);
    }
}
