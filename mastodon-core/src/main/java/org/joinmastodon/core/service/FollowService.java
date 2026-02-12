package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.repository.FollowRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final AccountService accountService;

    public FollowService(FollowRepository followRepository, AccountService accountService) {
        this.followRepository = followRepository;
        this.accountService = accountService;
    }

    @Transactional(readOnly = true)
    public Optional<Follow> findByAccountAndTarget(Account account, Account target) {
        return followRepository.findByAccountAndTargetAccount(account, target);
    }

    @Transactional(readOnly = true)
    public List<Follow> findFollowersWithCursor(Account target, Long maxId, Long sinceId, Pageable pageable) {
        return followRepository.findFollowersWithCursor(target, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Follow> findFollowingWithCursor(Account account, Long maxId, Long sinceId, Pageable pageable) {
        return followRepository.findFollowingWithCursor(account, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Follow> findByTargetAccount(Account target) {
        return followRepository.findByTargetAccount(target);
    }

    @Transactional(readOnly = true)
    public List<Follow> findByAccount(Account account) {
        return followRepository.findByAccount(account);
    }

    @Transactional
    public Follow save(Follow follow) {
        return followRepository.save(follow);
    }

    @Transactional
    public Follow follow(Account account, Account target) {
        Optional<Follow> existing = followRepository.findByAccountAndTargetAccount(account, target);
        if (existing.isPresent()) {
            return existing.get();
        }
        Follow follow = new Follow();
        follow.setAccount(account);
        follow.setTargetAccount(target);
        Follow saved = followRepository.save(follow);
        account.setFollowingCount(account.getFollowingCount() + 1);
        target.setFollowersCount(target.getFollowersCount() + 1);
        accountService.save(account);
        accountService.save(target);
        return saved;
    }

    @Transactional
    public void unfollow(Account account, Account target) {
        followRepository.findByAccountAndTargetAccount(account, target).ifPresent(follow -> {
            followRepository.delete(follow);
            account.setFollowingCount(Math.max(0, account.getFollowingCount() - 1));
            target.setFollowersCount(Math.max(0, target.getFollowersCount() - 1));
            accountService.save(account);
            accountService.save(target);
        });
    }
}
