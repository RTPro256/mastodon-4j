package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.repository.AccountRepository;
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
}
