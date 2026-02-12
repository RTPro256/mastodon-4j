package org.joinmastodon.media.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.jobs.JobService;
import org.joinmastodon.media.config.MediaProperties;
import org.joinmastodon.media.scanning.AvScanner;
import org.joinmastodon.media.scanning.AvScannerException;
import org.joinmastodon.media.storage.MediaStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaIngestionService {
    private final MediaStorage mediaStorage;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaProcessingService mediaProcessingService;
    private final MediaProperties properties;
    private final ObjectMapper objectMapper;
    private final JobService jobService;
    private final AvScanner avScanner;

    public MediaIngestionService(MediaStorage mediaStorage,
                                 MediaAttachmentService mediaAttachmentService,
                                 MediaProcessingService mediaProcessingService,
                                 MediaProperties properties,
                                 ObjectMapper objectMapper,
                                 JobService jobService,
                                 AvScanner avScanner) {
        this.mediaStorage = mediaStorage;
        this.mediaAttachmentService = mediaAttachmentService;
        this.mediaProcessingService = mediaProcessingService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.avScanner = avScanner;
    }

    public MediaAttachment ingest(Long accountId, MultipartFile file, String description, boolean async)
            throws IOException, AvScannerException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is required");
        }
        if (accountId == null) {
            throw new IOException("Account is required");
        }
        if (properties.getMaxUploadSize() != null
                && file.getSize() > properties.getMaxUploadSize().toBytes()) {
            throw new IOException("File exceeds max upload size");
        }
        String contentType = file.getContentType();
        if (properties.getAllowedTypes() != null && !properties.getAllowedTypes().isEmpty()) {
            if (contentType == null || properties.getAllowedTypes().stream()
                    .noneMatch(allowed -> allowed.equalsIgnoreCase(contentType))) {
                throw new IOException("Unsupported media type");
            }
        }

        MediaKind kind = MediaKind.fromContentType(contentType);
        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String key = "media/" + accountId + "/" + UUID.randomUUID() + extension;
        mediaStorage.save(key, file.getBytes());

        Path storedPath = mediaStorage.resolvePath(key);
        avScanner.scan(storedPath);

        MediaAttachment attachment = new MediaAttachment();
        attachment.setAccountId(accountId);
        attachment.setType(kind.getApiValue());
        attachment.setStorageKey(key);
        attachment.setContentType(contentType);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileSize(file.getSize());
        attachment.setDescription(description);
        attachment.setUrl(mediaStorage.resolveUrl(key));
        attachment.setProcessing(async || kind != MediaKind.IMAGE);
        attachment.setMetaJson(buildMeta(true));
        attachment.setProcessedAt(null);
        attachment.setCreatedAt(Instant.now());
        MediaAttachment saved = mediaAttachmentService.save(attachment);

        if (saved.isProcessing()) {
            enqueueProcessing(saved.getId());
        } else {
            mediaProcessingService.process(saved.getId());
        }
        return saved;
    }

    private void enqueueProcessing(Long mediaId) {
        MediaJobPayload payload = new MediaJobPayload(mediaId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            jobService.enqueue(MediaJobQueues.MEDIA_PROCESSING, json, Instant.now());
        } catch (JsonProcessingException ex) {
            jobService.enqueue(MediaJobQueues.MEDIA_PROCESSING,
                    "{\"mediaId\":" + mediaId + "}", Instant.now());
        }
    }

    private String buildMeta(boolean processing) throws JsonProcessingException {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processing", processing);
        return objectMapper.writeValueAsString(meta);
    }

    private String resolveExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        if (contentType == null) {
            return "";
        }
        if (contentType.equalsIgnoreCase("image/jpeg")) {
            return ".jpg";
        }
        if (contentType.equalsIgnoreCase("image/png")) {
            return ".png";
        }
        if (contentType.equalsIgnoreCase("image/gif")) {
            return ".gif";
        }
        if (contentType.equalsIgnoreCase("video/mp4")) {
            return ".mp4";
        }
        if (contentType.equalsIgnoreCase("video/webm")) {
            return ".webm";
        }
        if (contentType.equalsIgnoreCase("audio/mpeg")) {
            return ".mp3";
        }
        if (contentType.equalsIgnoreCase("audio/ogg")) {
            return ".ogg";
        }
        return "";
    }
}
