package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByAccount(Account account);

    List<Follow> findByTargetAccount(Account targetAccount);

    Optional<Follow> findByAccountAndTargetAccount(Account account, Account targetAccount);

    @Query("""
            select f from Follow f
            where f.targetAccount = :target
              and (:maxId is null or f.id < :maxId)
              and (:sinceId is null or f.id > :sinceId)
            order by f.id desc
            """)
    List<Follow> findFollowersWithCursor(
            @Param("target") Account target,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    @Query("""
            select f from Follow f
            where f.account = :account
              and (:maxId is null or f.id < :maxId)
              and (:sinceId is null or f.id > :sinceId)
            order by f.id desc
            """)
    List<Follow> findFollowingWithCursor(
            @Param("account") Account account,
            @Param("maxId") Long maxId,
            @Param("sinceId") Long sinceId,
            Pageable pageable);

    List<Follow> findByTargetAccountAndPendingTrue(Account targetAccount);

    Optional<Follow> findByAccountAndTargetAccountAndPendingTrue(Account account, Account targetAccount);
}
