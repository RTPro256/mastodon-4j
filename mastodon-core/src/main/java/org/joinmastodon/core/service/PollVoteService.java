package org.joinmastodon.core.service;

import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollOption;
import org.joinmastodon.core.entity.PollVote;
import org.joinmastodon.core.repository.PollOptionRepository;
import org.joinmastodon.core.repository.PollVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PollVoteService {
    private final PollVoteRepository pollVoteRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollService pollService;

    public PollVoteService(PollVoteRepository pollVoteRepository,
                           PollOptionRepository pollOptionRepository,
                           PollService pollService) {
        this.pollVoteRepository = pollVoteRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollService = pollService;
    }

    @Transactional
    public Poll castVotes(Poll poll, Account account, List<Integer> choices) {
        if (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Poll has expired");
        }
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("No choices provided");
        }
        if (!poll.isMultiple() && choices.size() > 1) {
            throw new IllegalStateException("Multiple choices not allowed");
        }
        if (pollVoteRepository.existsByPollAndAccountId(poll, account.getId())) {
            throw new IllegalStateException("Already voted");
        }

        List<PollOption> options = pollOptionRepository.findByPoll(poll);
        options.sort(java.util.Comparator.comparingInt(PollOption::getPosition));
        int votersCount = poll.getVotersCount() == null ? 0 : poll.getVotersCount();
        for (Integer choice : choices) {
            if (choice == null || choice < 0 || choice >= options.size()) {
                throw new IllegalStateException("Invalid choice");
            }
            PollOption option = options.get(choice);
            option.setVotesCount(option.getVotesCount() + 1);
            pollOptionRepository.save(option);

            PollVote vote = new PollVote();
            vote.setPoll(poll);
            vote.setAccountId(account.getId());
            vote.setPollOption(option);
            pollVoteRepository.save(vote);
        }

        poll.setVotesCount(poll.getVotesCount() + choices.size());
        poll.setVotersCount(votersCount + 1);
        return pollService.save(poll);
    }
}
