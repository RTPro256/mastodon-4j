package org.joinmastodon.core.repository;

import org.joinmastodon.core.entity.MediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
}
