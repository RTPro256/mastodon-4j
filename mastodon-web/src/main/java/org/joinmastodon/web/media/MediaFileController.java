package org.joinmastodon.web.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.joinmastodon.media.storage.MediaStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MediaFileController {
    private final MediaStorage mediaStorage;

    public MediaFileController(MediaStorage mediaStorage) {
        this.mediaStorage = mediaStorage;
    }

    @GetMapping("/media/{*path}")
    public ResponseEntity<byte[]> getMedia(@PathVariable("path") String path) {
        if (path.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media path");
        }
        try {
            byte[] data = mediaStorage.load(path);
            MediaType mediaType = resolveContentType(path);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                    .body(data);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
        }
    }

    private MediaType resolveContentType(String path) {
        try {
            Path resolved = mediaStorage.resolvePath(path);
            String type = Files.probeContentType(resolved);
            if (type != null) {
                return MediaType.parseMediaType(type);
            }
        } catch (Exception ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
