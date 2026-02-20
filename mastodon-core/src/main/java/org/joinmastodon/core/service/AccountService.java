package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByAcct(String acct) {
        return accountRepository.findByAcct(acct);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByUsernameAndDomain(String username, String domain) {
        return accountRepository.findByUsernameAndDomain(username, domain);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByActorUri(String actorUri) {
        return accountRepository.findByActorUri(actorUri);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findLocalAccountByUsername(String username) {
        return accountRepository.findByUsernameAndLocalAccountTrue(username);
    }

    @Transactional(readOnly = true)
    public java.util.List<Account> searchByUsernameOrAcct(String query) {
        return accountRepository.findByUsernameContainingIgnoreCaseOrAcctContainingIgnoreCase(query, query);
    }

    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    // Admin queries
    @Transactional(readOnly = true)
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Account> findBySuspended(boolean suspended, Pageable pageable) {
        return accountRepository.findBySuspended(suspended, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Account> findBySilenced(boolean silenced, Pageable pageable) {
        return accountRepository.findBySilenced(silenced, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Account> findByDisabled(boolean disabled, Pageable pageable) {
        return accountRepository.findByDisabled(disabled, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Account> findByDomain(String domain, Pageable pageable) {
        return accountRepository.findByDomain(domain, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Account> findByLocalAccount(boolean localAccount, Pageable pageable) {
        return accountRepository.findByLocalAccount(localAccount, pageable);
    }
}
