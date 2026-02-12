package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByAccountAndTargetAccount(Account account, Account targetAccount);

    List<Block> findByAccount(Account account);
}
