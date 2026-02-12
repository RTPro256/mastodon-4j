package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    List<PollVote> findByPollAndAccountId(Poll poll, Long accountId);

    boolean existsByPollAndAccountId(Poll poll, Long accountId);
}
