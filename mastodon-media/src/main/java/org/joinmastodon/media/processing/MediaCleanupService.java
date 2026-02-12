package org.joinmastodon.media.processing;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.media.config.MediaProperties;
import org.joinmastodon.media.storage.MediaStorage;
import org.springframework.stereotype.Service;

@Service
public class MediaCleanupService {
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaStorage mediaStorage;
    private final MediaProperties properties;

    public MediaCleanupService(MediaAttachmentService mediaAttachmentService,
                               MediaStorage mediaStorage,
                               MediaProperties properties) {
        this.mediaAttachmentService = mediaAttachmentService;
        this.mediaStorage = mediaStorage;
        this.properties = properties;
    }

    public int cleanupOrphaned() throws IOException {
        Instant cutoff = Instant.now().minus(properties.getCleanupRetention());
        List<MediaAttachment> attachments = mediaAttachmentService.findOrphanedBefore(
                cutoff,
                properties.getCleanupBatchSize());
        int removed = 0;
        for (MediaAttachment attachment : attachments) {
            deleteAttachment(attachment);
            removed++;
        }
        return removed;
    }

    private void deleteAttachment(MediaAttachment attachment) throws IOException {
        if (attachment.getStorageKey() != null) {
            mediaStorage.delete(attachment.getStorageKey());
        }
        if (attachment.getPreviewKey() != null) {
            mediaStorage.delete(attachment.getPreviewKey());
        }
        mediaAttachmentService.delete(attachment);
    }
}
