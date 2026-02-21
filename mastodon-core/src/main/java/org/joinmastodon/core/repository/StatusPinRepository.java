package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.StatusPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusPinRepository extends JpaRepository<StatusPin, Long> {
    List<StatusPin> findByAccountOrderByCreatedAtDesc(Account account);

    Optional<StatusPin> findByAccountAndStatus(Account account, Status status);

    boolean existsByAccountAndStatus(Account account, Status status);

    void deleteByAccountAndStatus(Account account, Status status);
}
