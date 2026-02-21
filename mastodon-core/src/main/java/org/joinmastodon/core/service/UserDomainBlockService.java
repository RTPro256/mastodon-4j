package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.UserDomainBlock;
import org.joinmastodon.core.repository.UserDomainBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDomainBlockService {
    private final UserDomainBlockRepository userDomainBlockRepository;

    public UserDomainBlockService(UserDomainBlockRepository userDomainBlockRepository) {
        this.userDomainBlockRepository = userDomainBlockRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDomainBlock> findByAccount(Account account) {
        return userDomainBlockRepository.findByAccount(account);
    }

    @Transactional(readOnly = true)
    public Optional<UserDomainBlock> findByAccountAndDomain(Account account, String domain) {
        return userDomainBlockRepository.findByAccountAndDomain(account, domain);
    }

    @Transactional(readOnly = true)
    public boolean isDomainBlocked(Account account, String domain) {
        return userDomainBlockRepository.existsByAccountAndDomain(account, domain);
    }

    @Transactional
    public UserDomainBlock blockDomain(Account account, String domain) {
        if (isDomainBlocked(account, domain)) {
            return userDomainBlockRepository.findByAccountAndDomain(account, domain).orElseThrow();
        }
        UserDomainBlock block = new UserDomainBlock();
        block.setAccount(account);
        block.setDomain(domain.toLowerCase());
        return userDomainBlockRepository.save(block);
    }

    @Transactional
    public void unblockDomain(Account account, String domain) {
        userDomainBlockRepository.deleteByAccountAndDomain(account, domain.toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<String> getBlockedDomains(Account account) {
        return userDomainBlockRepository.findByAccount(account).stream()
                .map(UserDomainBlock::getDomain)
                .toList();
    }
}
