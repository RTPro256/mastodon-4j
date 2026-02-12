package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Optional<Favourite> findByAccountAndStatus(Account account, Status status);

    List<Favourite> findByAccount(Account account);
}
