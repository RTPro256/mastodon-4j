package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Mention;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusVisibilityService {
    private final FollowRepository followRepository;

    public StatusVisibilityService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public boolean canView(Status status, Account viewer) {
        if (status == null) {
            return false;
        }
        Visibility visibility = status.getVisibility();
        if (visibility == null || visibility == Visibility.PUBLIC || visibility == Visibility.UNLISTED) {
            return true;
        }
        if (viewer == null) {
            return false;
        }
        if (status.getAccount() != null && status.getAccount().getId().equals(viewer.getId())) {
            return true;
        }
        if (visibility == Visibility.PRIVATE) {
            return followRepository.findByAccountAndTargetAccount(viewer, status.getAccount()).isPresent();
        }
        if (visibility == Visibility.DIRECT) {
            if (status.getMentions() != null) {
                for (Mention mention : status.getMentions()) {
                    if (mention.getAccount() != null && mention.getAccount().getId().equals(viewer.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
