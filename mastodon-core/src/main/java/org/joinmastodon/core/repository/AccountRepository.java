package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAcct(String acct);
    Optional<Account> findByUsernameAndDomain(String username, String domain);

    List<Account> findByUsernameContainingIgnoreCaseOrAcctContainingIgnoreCase(String username, String acct);
}
