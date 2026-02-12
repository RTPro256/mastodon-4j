package org.joinmastodon.web.api;

import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.service.PollService;
import org.joinmastodon.web.api.dto.PollDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/polls")
public class PollController {
    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping("/{id}")
    public PollDto getPoll(@PathVariable("id") String id) {
        Poll poll = pollService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found"));
        return ApiMapper.toPollDto(poll);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }
}
