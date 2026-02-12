package org.joinmastodon.media.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.media.config.MediaProperties;
import org.joinmastodon.media.scanning.AvScanner;
import org.joinmastodon.media.scanning.AvScannerException;
import org.joinmastodon.media.storage.MediaStorage;
import org.springframework.stereotype.Service;

@Service
public class MediaProcessingService {
    private final MediaStorage mediaStorage;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaProperties properties;
    private final ObjectMapper objectMapper;
    private final AvScanner avScanner;
    private final FfmpegService ffmpegService;

    public MediaProcessingService(MediaStorage mediaStorage,
                                  MediaAttachmentService mediaAttachmentService,
                                  MediaProperties properties,
                                  ObjectMapper objectMapper,
                                  AvScanner avScanner,
                                  FfmpegService ffmpegService) {
        this.mediaStorage = mediaStorage;
        this.mediaAttachmentService = mediaAttachmentService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.avScanner = avScanner;
        this.ffmpegService = ffmpegService;
    }

    public MediaAttachment process(Long mediaId) {
        MediaAttachment attachment = mediaAttachmentService.findById(mediaId)
                .orElseThrow(() -> new IllegalStateException("Media not found"));
        try {
            processAttachment(attachment);
        } catch (Exception ex) {
            attachment.setMetaJson(buildMeta(true));
            attachment.setProcessing(true);
            mediaAttachmentService.save(attachment);
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return attachment;
    }

    public void processAttachment(MediaAttachment attachment)
            throws IOException, AvScannerException, InterruptedException {
        Path sourcePath = mediaStorage.resolvePath(attachment.getStorageKey());
        avScanner.scan(sourcePath);

        MediaKind kind = MediaKind.fromContentType(attachment.getContentType());
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processing", false);

        if (kind == MediaKind.IMAGE) {
            processImage(attachment, sourcePath, meta);
        } else if (kind == MediaKind.VIDEO) {
            processVideo(attachment, sourcePath, meta);
        } else {
            meta.put("original", Map.of("size", attachment.getFileSize()));
        }

        attachment.setProcessing(false);
        attachment.setProcessedAt(Instant.now());
        attachment.setMetaJson(writeMeta(meta));
        mediaAttachmentService.save(attachment);
    }

    private void processImage(MediaAttachment attachment, Path sourcePath, Map<String, Object> meta)
            throws IOException {
        var image = javax.imageio.ImageIO.read(sourcePath.toFile());
        if (image == null) {
            throw new IOException("Unable to read image");
        }
        int width = image.getWidth();
        int height = image.getHeight();
        meta.put("original", Map.of(
                "width", width,
                "height", height,
                "size", attachment.getFileSize()
        ));

        ByteArrayOutputStream previewOut = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(properties.getPreviewMaxWidth(), properties.getPreviewMaxHeight())
                .outputFormat("jpg")
                .toOutputStream(previewOut);
        String previewKey = attachment.getStorageKey() + "_preview.jpg";
        mediaStorage.save(previewKey, previewOut.toByteArray());

        attachment.setPreviewKey(previewKey);
        attachment.setPreviewUrl(mediaStorage.resolveUrl(previewKey));
        meta.put("small", Map.of(
                "width", properties.getPreviewMaxWidth(),
                "height", properties.getPreviewMaxHeight()
        ));
    }

    private void processVideo(MediaAttachment attachment, Path sourcePath, Map<String, Object> meta)
            throws IOException, InterruptedException {
        meta.put("original", Map.of("size", attachment.getFileSize()));

        if (ffmpegService.isAvailable()) {
            Path previewFile = Files.createTempFile("mastodon-preview-", ".jpg");
            try {
                ffmpegService.generateVideoPreview(sourcePath, previewFile);
                byte[] data = Files.readAllBytes(previewFile);
                String previewKey = attachment.getStorageKey() + "_preview.jpg";
                mediaStorage.save(previewKey, data);
                attachment.setPreviewKey(previewKey);
                attachment.setPreviewUrl(mediaStorage.resolveUrl(previewKey));
            } finally {
                Files.deleteIfExists(previewFile);
            }
        }

        if (properties.isTranscodeEnabled() && ffmpegService.isAvailable()) {
            Path transcodeFile = Files.createTempFile("mastodon-transcode-", ".mp4");
            try {
                ffmpegService.transcodeToMp4(sourcePath, transcodeFile);
                byte[] data = Files.readAllBytes(transcodeFile);
                String transcodedKey = attachment.getStorageKey() + "_transcoded.mp4";
                mediaStorage.save(transcodedKey, data);
                attachment.setStorageKey(transcodedKey);
                attachment.setUrl(mediaStorage.resolveUrl(transcodedKey));
            } finally {
                Files.deleteIfExists(transcodeFile);
            }
        }
    }

    private String buildMeta(boolean processing) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("processing", processing);
        return writeMeta(meta);
    }

    private String writeMeta(Map<String, Object> meta) {
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
