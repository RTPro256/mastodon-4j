package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Bookmark;
import org.joinmastodon.core.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByAccountAndStatus(Account account, Status status);

    List<Bookmark> findByAccount(Account account);
}
