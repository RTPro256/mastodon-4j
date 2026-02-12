package org.joinmastodon.core.repository;

import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.MediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, Long> {
    @Query(value = """
            select * from media_attachments m
            where m.created_at < :cutoff
              and not exists (
                select 1 from status_media_attachments sma
                where sma.media_attachment_id = m.id
              )
            order by m.created_at asc
            limit :limit
            """, nativeQuery = true)
    List<MediaAttachment> findOrphanedBefore(@Param("cutoff") Instant cutoff, @Param("limit") int limit);
}
