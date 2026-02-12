package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {
    Optional<Poll> findByStatusId(Long statusId);
}
