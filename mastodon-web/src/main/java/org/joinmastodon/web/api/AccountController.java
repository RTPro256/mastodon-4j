package org.joinmastodon.web.api;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.StatusDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/accounts")
public class AccountController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final AccountService accountService;
    private final StatusService statusService;

    public AccountController(AccountService accountService, StatusService statusService) {
        this.accountService = accountService;
        this.statusService = statusService;
    }

    @GetMapping("/{id}")
    public AccountDto getAccount(@PathVariable("id") String id) {
        Account account = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return ApiMapper.toAccountDto(account);
    }

    @GetMapping("/{id}/statuses")
    public ResponseEntity<List<StatusDto>> getAccountStatuses(
            @PathVariable("id") String id,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account account = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(0, resolvedLimit);
        Long max = parseOptionalIdValue(maxId);
        Long since = parseOptionalIdValue(sinceId);
        List<StatusDto> statuses = statusService.findByAccountWithCursor(account, max, since, pageable)
                .stream()
                .map(ApiMapper::toStatusDto)
                .toList();

        String basePath = ApiVersion.V1 + "/accounts/" + account.getId() + "/statuses?limit=" + resolvedLimit;
        List<Long> ids = statuses.stream()
                .map(StatusDto::id)
                .map(this::parseOptionalId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        Long nextMaxId = PaginationUtil.nextMaxId(ids);
        Long prevSinceId = PaginationUtil.prevSinceId(ids);
        String link = LinkHeaderBuilder.build(basePath, nextMaxId, prevSinceId);
        if (link == null) {
            return ResponseEntity.ok(statuses);
        }
        return ResponseEntity.ok()
                .header("Link", link)
                .body(statuses);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    private Optional<Long> parseOptionalId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parseId(id));
    }

    private Long parseOptionalIdValue(String id) {
        return parseOptionalId(id).orElse(null);
    }
}
