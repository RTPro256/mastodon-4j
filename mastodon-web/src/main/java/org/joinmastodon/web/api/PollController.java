package org.joinmastodon.web.api;

import jakarta.validation.Valid;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.PollService;
import org.joinmastodon.core.service.PollVoteService;
import org.joinmastodon.web.api.dto.PollDto;
import org.joinmastodon.web.api.dto.request.PollVoteRequest;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/polls")
public class PollController {
    private static final HttpStatusCode UNPROCESSABLE_ENTITY = HttpStatusCode.valueOf(422);
    private final PollService pollService;
    private final PollVoteService pollVoteService;
    private final AccountService accountService;

    public PollController(PollService pollService, PollVoteService pollVoteService, AccountService accountService) {
        this.pollService = pollService;
        this.pollVoteService = pollVoteService;
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public PollDto getPoll(@PathVariable("id") String id) {
        Poll poll = pollService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found"));
        return ApiMapper.toPollDto(poll);
    }

    @PostMapping("/{id}/votes")
    public PollDto vote(@PathVariable("id") String id, @Valid @RequestBody PollVoteRequest request) {
        Poll poll = pollService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found"));
        Account account = requireAccount();
        try {
            Poll updated = pollVoteService.castVotes(poll, account, request.choices());
            return ApiMapper.toPollDto(updated);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, ex.getMessage());
        }
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    private Account requireAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.accountId() != null) {
                return accountService.findById(principal.accountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
