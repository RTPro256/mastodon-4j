package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.repository.MediaAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaAttachmentService {
    private final MediaAttachmentRepository mediaAttachmentRepository;

    public MediaAttachmentService(MediaAttachmentRepository mediaAttachmentRepository) {
        this.mediaAttachmentRepository = mediaAttachmentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MediaAttachment> findById(Long id) {
        return mediaAttachmentRepository.findById(id);
    }

    @Transactional
    public MediaAttachment save(MediaAttachment attachment) {
        return mediaAttachmentRepository.save(attachment);
    }
}
