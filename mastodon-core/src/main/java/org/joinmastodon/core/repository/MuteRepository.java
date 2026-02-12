package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Mute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuteRepository extends JpaRepository<Mute, Long> {
    Optional<Mute> findByAccountAndTargetAccount(Account account, Account targetAccount);

    List<Mute> findByAccount(Account account);
}
