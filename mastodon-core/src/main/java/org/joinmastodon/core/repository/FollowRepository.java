package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByAccountAndTargetAccount(Account account, Account targetAccount);
}
