package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPoll(Poll poll);
}
