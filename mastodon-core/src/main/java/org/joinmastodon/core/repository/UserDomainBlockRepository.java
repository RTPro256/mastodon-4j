package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.UserDomainBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDomainBlockRepository extends JpaRepository<UserDomainBlock, Long> {
    List<UserDomainBlock> findByAccount(Account account);

    Optional<UserDomainBlock> findByAccountAndDomain(Account account, String domain);

    boolean existsByAccountAndDomain(Account account, String domain);

    void deleteByAccountAndDomain(Account account, String domain);
}
