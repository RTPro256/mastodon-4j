package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    @Query("""
            select s from Status s
            where s.account = :account
              and (:maxId is null or s.id < :maxId)
              and (:sinceId is null or s.id > :sinceId)
            order by s.id desc
            """)
    List<Status> findByAccountWithCursor(
            @Param("account") Account account,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    @Query("""
            select s from Status s
            where s.visibility in :visibilities
              and (:maxId is null or s.id < :maxId)
              and (:sinceId is null or s.id > :sinceId)
            order by s.id desc
            """)
    List<Status> findPublicTimeline(
            @Param("visibilities") List<Visibility> visibilities,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    @Query("""
            select s from Status s
            where (s.account = :account
                or s.account in (select f.targetAccount from Follow f where f.account = :account))
              and s.visibility <> org.joinmastodon.core.model.Visibility.DIRECT
              and (:maxId is null or s.id < :maxId)
              and (:sinceId is null or s.id > :sinceId)
            order by s.id desc
            """)
    List<Status> findHomeTimeline(
            @Param("account") Account account,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    @Query("""
            select s from Status s
            join s.tags t
            where lower(t.name) = lower(:tag)
              and (:maxId is null or s.id < :maxId)
              and (:sinceId is null or s.id > :sinceId)
            order by s.id desc
            """)
    List<Status> findByTag(
            @Param("tag") String tag,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    @Query("""
            select s from Status s
            where s.account in (select la.account from ListAccount la where la.list = :list)
              and (:maxId is null or s.id < :maxId)
              and (:sinceId is null or s.id > :sinceId)
            order by s.id desc
            """)
    List<Status> findByList(
            @Param("list") org.joinmastodon.core.entity.ListEntity list,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    List<Status> findByContentContainingIgnoreCase(String content, Pageable pageable);

    Optional<Status> findByAccountAndReblog(Account account, Status reblog);

    List<Status> findByInReplyToIdOrderByIdAsc(Long inReplyToId);

    Optional<Status> findByUri(String uri);

    // Full-text search using PostgreSQL tsvector
    @Query(value = """
            SELECT s.* FROM statuses s
            WHERE s.content_tsvector @@ plainto_tsquery('english', :query)
            ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<Status> searchByContent(@Param("query") String query, Pageable pageable);

    // Fallback search using LIKE for when tsvector doesn't return results
    @Query("""
            SELECT s FROM Status s
            WHERE LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY s.createdAt DESC
            """)
    List<Status> searchByContentLike(@Param("query") String query, Pageable pageable);
}
