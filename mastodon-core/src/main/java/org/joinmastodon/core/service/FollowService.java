package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Follow> findByAccountAndTarget(Account account, Account target) {
        return followRepository.findByAccountAndTargetAccount(account, target);
    }

    @Transactional
    public Follow save(Follow follow) {
        return followRepository.save(follow);
    }
}
