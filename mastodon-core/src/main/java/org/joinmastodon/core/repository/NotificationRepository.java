package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    @Query("""
            select n from Notification n
            where n.account = :account
              and (:maxId is null or n.id < :maxId)
              and (:sinceId is null or n.id > :sinceId)
            order by n.id desc
            """)
    List<Notification> findByAccountWithCursor(
            @Param("account") Account account,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);
}
