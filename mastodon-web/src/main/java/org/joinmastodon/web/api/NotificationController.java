package org.joinmastodon.web.api;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Notification;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.NotificationService;
import org.joinmastodon.web.api.dto.NotificationDto;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/notifications")
public class NotificationController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final AccountService accountService;
    private final NotificationService notificationService;

    public NotificationController(AccountService accountService, NotificationService notificationService) {
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @RequestParam(value = "account_id", required = false) String accountId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account account = accountId == null ? requireAccount() : accountService.findById(parseId(accountId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(0, resolvedLimit);
        Long max = parseOptionalIdValue(maxId);
        Long since = parseOptionalIdValue(sinceId);

        List<NotificationDto> notifications = notificationService
                .findByAccountWithCursor(account, max, since, pageable)
                .stream()
                .map(ApiMapper::toNotificationDto)
                .toList();

        String basePath = ApiVersion.V1 + "/notifications?account_id=" + account.getId() + "&limit=" + resolvedLimit;
        List<Long> ids = notifications.stream()
                .map(NotificationDto::id)
                .map(this::parseOptionalId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        Long nextMaxId = PaginationUtil.nextMaxId(ids);
        Long prevSinceId = PaginationUtil.prevSinceId(ids);
        String link = LinkHeaderBuilder.build(basePath, nextMaxId, prevSinceId);
        if (link == null) {
            return ResponseEntity.ok(notifications);
        }
        return ResponseEntity.ok()
                .header("Link", link)
                .body(notifications);
    }

    @GetMapping("/{id}")
    public NotificationDto getNotification(@PathVariable("id") String id) {
        Account account = requireAccount();
        Notification notification = notificationService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return ApiMapper.toNotificationDto(notification);
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearNotifications() {
        Account account = requireAccount();
        notificationService.clear(account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<Void> dismissNotification(@PathVariable("id") String id) {
        Account account = requireAccount();
        Notification notification = notificationService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        notificationService.delete(notification);
        return ResponseEntity.ok().build();
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
