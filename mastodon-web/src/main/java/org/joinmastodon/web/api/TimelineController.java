package org.joinmastodon.web.api;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.ListEntity;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.ListService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.web.api.dto.StatusDto;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/timelines")
public class TimelineController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final StatusService statusService;
    private final AccountService accountService;
    private final ListService listService;

    public TimelineController(StatusService statusService, AccountService accountService, ListService listService) {
        this.statusService = statusService;
        this.accountService = accountService;
        this.listService = listService;
    }

    @GetMapping("/home")
    public ResponseEntity<List<StatusDto>> homeTimeline(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account account = requireAccount();
        return timelineResponse(
                statusService.findHomeTimeline(account, parseOptionalId(maxId), parseOptionalId(sinceId), pageable(limit)),
                ApiVersion.V1 + "/timelines/home",
                limit);
    }

    @GetMapping("/public")
    public ResponseEntity<List<StatusDto>> publicTimeline(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        List<Status> statuses = statusService.findPublicTimeline(
                List.of(Visibility.PUBLIC, Visibility.UNLISTED),
                parseOptionalId(maxId),
                parseOptionalId(sinceId),
                pageable(limit));
        return timelineResponse(statuses, ApiVersion.V1 + "/timelines/public", limit);
    }

    @GetMapping("/tag/{hashtag}")
    public ResponseEntity<List<StatusDto>> tagTimeline(
            @PathVariable("hashtag") String hashtag,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        List<Status> statuses = statusService.findByTag(
                hashtag,
                parseOptionalId(maxId),
                parseOptionalId(sinceId),
                pageable(limit)).stream()
                .filter(status -> status.getVisibility() == Visibility.PUBLIC || status.getVisibility() == Visibility.UNLISTED)
                .toList();
        return timelineResponse(statuses, ApiVersion.V1 + "/timelines/tag/" + hashtag, limit);
    }

    @GetMapping("/list/{list_id}")
    public ResponseEntity<List<StatusDto>> listTimeline(
            @PathVariable("list_id") String listId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(listId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        if (!list.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        List<Status> statuses = statusService.findByList(
                list,
                parseOptionalId(maxId),
                parseOptionalId(sinceId),
                pageable(limit));
        return timelineResponse(statuses, ApiVersion.V1 + "/timelines/list/" + list.getId(), limit);
    }

    private ResponseEntity<List<StatusDto>> timelineResponse(List<Status> statuses, String basePath, Integer limit) {
        List<StatusDto> body = statuses.stream().map(ApiMapper::toStatusDto).toList();
        List<Long> ids = body.stream()
                .map(StatusDto::id)
                .map(this::parseOptionalId)
                .filter(java.util.Objects::nonNull)
                .toList();
        Long nextMaxId = PaginationUtil.nextMaxId(ids);
        Long prevSinceId = PaginationUtil.prevSinceId(ids);
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        String link = LinkHeaderBuilder.build(basePath + "?limit=" + resolvedLimit, nextMaxId, prevSinceId);
        if (link == null) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok().header("Link", link).body(body);
    }

    private Pageable pageable(Integer limit) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        return PageRequest.of(0, resolvedLimit);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    private Long parseOptionalId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return parseId(id);
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
