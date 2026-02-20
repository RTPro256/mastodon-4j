package org.joinmastodon.web.api.admin;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.AccountAction;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.ModerationService;
import org.joinmastodon.web.api.ApiMapper;
import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.auth.AdminOnly;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/admin/accounts")
@AdminOnly(moderator = true)
public class AdminAccountController {

    private final AccountService accountService;
    private final ModerationService moderationService;

    public AdminAccountController(AccountService accountService, ModerationService moderationService) {
        this.accountService = accountService;
        this.moderationService = moderationService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> listAccounts(
            @RequestParam(value = "local", required = false) Boolean local,
            @RequestParam(value = "remote", required = false) Boolean remote,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "pending", required = false) Boolean pending,
            @RequestParam(value = "disabled", required = false) Boolean disabled,
            @RequestParam(value = "silenced", required = false) Boolean silenced,
            @RequestParam(value = "suspended", required = false) Boolean suspended,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "display_name", required = false) String displayName,
            @RequestParam(value = "by_domain", required = false) String domain,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        
        int resolvedLimit = limit == null ? 40 : Math.min(Math.max(limit, 1), 80);
        Pageable pageable = PageRequest.of(0, resolvedLimit, Sort.by("createdAt").descending());
        
        Page<Account> accounts;
        if (suspended != null && suspended) {
            accounts = accountService.findBySuspended(true, pageable);
        } else if (silenced != null && silenced) {
            accounts = accountService.findBySilenced(true, pageable);
        } else if (disabled != null && disabled) {
            accounts = accountService.findByDisabled(true, pageable);
        } else if (domain != null && !domain.isBlank()) {
            accounts = accountService.findByDomain(domain, pageable);
        } else if (local != null && local) {
            accounts = accountService.findByLocalAccount(true, pageable);
        } else {
            accounts = accountService.findAll(pageable);
        }
        
        List<AccountDto> result = accounts.getContent().stream()
                .map(ApiMapper::toAccountDto)
                .toList();
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable("id") String id) {
        Account account = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return ResponseEntity.ok(ApiMapper.toAccountDto(account));
    }

    @PostMapping("/{id}/action")
    public ResponseEntity<AccountActionDto> performAction(
            @PathVariable("id") String id,
            @RequestBody AccountActionRequest request) {
        
        Account targetAccount = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        
        Account actionBy = getCurrentAccount();
        
        AccountAction action;
        AccountActionType actionType = AccountActionType.valueOf(request.type().toUpperCase());
        switch (actionType) {
            case SUSPEND:
                action = moderationService.suspendAccount(targetAccount, actionBy, request.text(), null);
                break;
            case SILENCE:
                action = moderationService.silenceAccount(targetAccount, actionBy, request.text(), null);
                break;
            case DISABLE:
                action = moderationService.disableAccount(targetAccount, actionBy, request.text(), null);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action type: " + request.type());
        }
        
        return ResponseEntity.ok(toAccountActionDto(action));
    }

    @PostMapping("/{id}/unsuspend")
    public ResponseEntity<AccountDto> unsuspendAccount(@PathVariable("id") String id) {
        Account targetAccount = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        
        Account actionBy = getCurrentAccount();
        moderationService.unsuspendAccount(targetAccount, actionBy, "Unsuspended via admin API");
        
        return ResponseEntity.ok(ApiMapper.toAccountDto(targetAccount));
    }

    @PostMapping("/{id}/unsilence")
    public ResponseEntity<AccountDto> unsilenceAccount(@PathVariable("id") String id) {
        Account targetAccount = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        
        Account actionBy = getCurrentAccount();
        moderationService.unsilenceAccount(targetAccount, actionBy, "Unsilenced via admin API");
        
        return ResponseEntity.ok(ApiMapper.toAccountDto(targetAccount));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<AccountDto> enableAccount(@PathVariable("id") String id) {
        Account targetAccount = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        
        Account actionBy = getCurrentAccount();
        moderationService.enableAccount(targetAccount, actionBy, "Enabled via admin API");
        
        return ResponseEntity.ok(ApiMapper.toAccountDto(targetAccount));
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return accountService.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    private AccountActionDto toAccountActionDto(AccountAction action) {
        return new AccountActionDto(
                String.valueOf(action.getId()),
                action.getActionType().name().toLowerCase(),
                action.getCreatedAt().toString(),
                action.getReason(),
                action.getTargetAccount() != null ? String.valueOf(action.getTargetAccount().getId()) : null,
                action.getActionTakenBy() != null ? String.valueOf(action.getActionTakenBy().getId()) : null
        );
    }

    // DTOs
    public enum AccountActionType {
        SUSPEND,
        SILENCE,
        DISABLE
    }

    public record AccountActionRequest(
            String type,
            String text,
            Long reportId) {}

    public record AccountActionDto(
            String id,
            String type,
            String createdAt,
            String reason,
            String targetAccountId,
            String actionTakenByAccountId) {}
}
