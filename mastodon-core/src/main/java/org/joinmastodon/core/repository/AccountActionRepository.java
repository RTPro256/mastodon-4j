package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.AccountAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountActionRepository extends JpaRepository<AccountAction, Long> {
    
    List<AccountAction> findByTargetAccount(Account targetAccount);
    
    Page<AccountAction> findByTargetAccountOrderByCreatedAtDesc(Account targetAccount, Pageable pageable);
    
    List<AccountAction> findByActionTakenByOrderByCreatedAtDesc(Account actionTakenBy);
    
    Page<AccountAction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
