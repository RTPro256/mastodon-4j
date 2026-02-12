package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.repository.PollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PollService {
    private final PollRepository pollRepository;

    public PollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Poll> findById(Long id) {
        return pollRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Poll> findByStatusId(Long statusId) {
        return pollRepository.findByStatusId(statusId);
    }

    @Transactional
    public Poll save(Poll poll) {
        return pollRepository.save(poll);
    }
}
