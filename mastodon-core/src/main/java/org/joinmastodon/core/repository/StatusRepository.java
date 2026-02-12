package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
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
}
