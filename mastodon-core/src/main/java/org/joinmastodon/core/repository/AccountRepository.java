package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAcct(String acct);
    Optional<Account> findByUsernameAndDomain(String username, String domain);
    Optional<Account> findByActorUri(String actorUri);
    Optional<Account> findByUsernameAndLocalAccountTrue(String username);

    List<Account> findByUsernameContainingIgnoreCaseOrAcctContainingIgnoreCase(String username, String acct);
    
    // Admin queries
    Page<Account> findBySuspended(boolean suspended, Pageable pageable);
    Page<Account> findBySilenced(boolean silenced, Pageable pageable);
    Page<Account> findByDisabled(boolean disabled, Pageable pageable);
    Page<Account> findByDomain(String domain, Pageable pageable);
    Page<Account> findByLocalAccount(boolean localAccount, Pageable pageable);
}
