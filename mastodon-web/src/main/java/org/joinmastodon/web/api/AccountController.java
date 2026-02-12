package org.joinmastodon.web.api;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Block;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.entity.Mute;
import org.joinmastodon.core.service.BlockService;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.FollowService;
import org.joinmastodon.core.service.MuteService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.core.service.StatusVisibilityService;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.RelationshipDto;
import org.joinmastodon.web.api.dto.StatusDto;
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
@RequestMapping(ApiVersion.V1 + "/accounts")
public class AccountController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final AccountService accountService;
    private final StatusService statusService;
    private final StatusVisibilityService statusVisibilityService;
    private final FollowService followService;
    private final BlockService blockService;
    private final MuteService muteService;

    public AccountController(AccountService accountService,
                             StatusService statusService,
                             FollowService followService,
                             BlockService blockService,
                             MuteService muteService,
                             StatusVisibilityService statusVisibilityService) {
        this.accountService = accountService;
        this.statusService = statusService;
        this.followService = followService;
        this.blockService = blockService;
        this.muteService = muteService;
        this.statusVisibilityService = statusVisibilityService;
    }

    @GetMapping("/{id}")
    public AccountDto getAccount(@PathVariable("id") String id) {
        Account account = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return ApiMapper.toAccountDto(account);
    }

    @GetMapping("/verify_credentials")
    public AccountDto verifyCredentials() {
        AuthenticatedPrincipal principal = currentPrincipal();
        if (principal.accountId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Account account = accountService.findById(principal.accountId())
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
        Account viewer = currentAccountOrNull();
        List<StatusDto> statuses = statusService.findByAccountWithCursor(account, max, since, pageable)
                .stream()
                .filter(status -> statusVisibilityService.canView(status, viewer))
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

    @GetMapping("/search")
    public List<AccountDto> searchAccounts(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", required = false) Integer limit) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        return accountService.searchByUsernameOrAcct(query).stream()
                .limit(resolvedLimit)
                .map(ApiMapper::toAccountDto)
                .toList();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<AccountDto>> getFollowers(
            @PathVariable("id") String id,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(0, resolvedLimit);
        List<Follow> follows = followService.findFollowersWithCursor(
                target, parseOptionalIdValue(maxId), parseOptionalIdValue(sinceId), pageable);
        List<AccountDto> body = follows.stream()
                .map(Follow::getAccount)
                .map(ApiMapper::toAccountDto)
                .toList();
        List<Long> ids = follows.stream().map(Follow::getId).toList();
        String basePath = ApiVersion.V1 + "/accounts/" + target.getId() + "/followers?limit=" + resolvedLimit;
        return pagedAccounts(body, ids, basePath);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<AccountDto>> getFollowing(
            @PathVariable("id") String id,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        Account account = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(0, resolvedLimit);
        List<Follow> follows = followService.findFollowingWithCursor(
                account, parseOptionalIdValue(maxId), parseOptionalIdValue(sinceId), pageable);
        List<AccountDto> body = follows.stream()
                .map(Follow::getTargetAccount)
                .map(ApiMapper::toAccountDto)
                .toList();
        List<Long> ids = follows.stream().map(Follow::getId).toList();
        String basePath = ApiVersion.V1 + "/accounts/" + account.getId() + "/following?limit=" + resolvedLimit;
        return pagedAccounts(body, ids, basePath);
    }

    @GetMapping("/relationships")
    public List<RelationshipDto> relationships(
            @RequestParam(value = "id[]", required = false) List<String> ids,
            @RequestParam(value = "id", required = false) List<String> idsFallback) {
        Account account = requireAccount();
        List<String> resolved = ids != null && !ids.isEmpty() ? ids : idsFallback;
        if (resolved == null || resolved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing id");
        }
        return resolved.stream()
                .map(this::parseId)
                .map(id -> accountService.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
                .map(target -> buildRelationship(account, target))
                .toList();
    }

    @PostMapping("/{id}/follow")
    public RelationshipDto follow(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot follow self");
        }
        followService.follow(account, target);
        return buildRelationship(account, target);
    }

    @PostMapping("/{id}/unfollow")
    public RelationshipDto unfollow(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        followService.unfollow(account, target);
        return buildRelationship(account, target);
    }

    @PostMapping("/{id}/block")
    public RelationshipDto block(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (blockService.findByAccountAndTarget(account, target).isEmpty()) {
            Block block = new Block();
            block.setAccount(account);
            block.setTargetAccount(target);
            blockService.save(block);
        }
        followService.unfollow(account, target);
        return buildRelationship(account, target);
    }

    @PostMapping("/{id}/unblock")
    public RelationshipDto unblock(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        blockService.findByAccountAndTarget(account, target).ifPresent(blockService::delete);
        return buildRelationship(account, target);
    }

    @PostMapping("/{id}/mute")
    public RelationshipDto mute(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (muteService.findByAccountAndTarget(account, target).isEmpty()) {
            Mute mute = new Mute();
            mute.setAccount(account);
            mute.setTargetAccount(target);
            muteService.save(mute);
        }
        return buildRelationship(account, target);
    }

    @PostMapping("/{id}/unmute")
    public RelationshipDto unmute(@PathVariable("id") String id) {
        Account account = requireAccount();
        Account target = accountService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        muteService.findByAccountAndTarget(account, target).ifPresent(muteService::delete);
        return buildRelationship(account, target);
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

    private ResponseEntity<List<AccountDto>> pagedAccounts(List<AccountDto> body, List<Long> ids, String basePath) {
        Long nextMaxId = PaginationUtil.nextMaxId(ids);
        Long prevSinceId = PaginationUtil.prevSinceId(ids);
        String link = LinkHeaderBuilder.build(basePath, nextMaxId, prevSinceId);
        if (link == null) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok().header("Link", link).body(body);
    }

    private RelationshipDto buildRelationship(Account source, Account target) {
        boolean following = followService.findByAccountAndTarget(source, target).isPresent();
        boolean followedBy = followService.findByAccountAndTarget(target, source).isPresent();
        boolean blocking = blockService.findByAccountAndTarget(source, target).isPresent();
        boolean muting = muteService.findByAccountAndTarget(source, target).isPresent();
        return new RelationshipDto(
                Long.toString(target.getId()),
                following,
                followedBy,
                blocking,
                muting
        );
    }

    private AuthenticatedPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            return principal;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private Account requireAccount() {
        AuthenticatedPrincipal principal = currentPrincipal();
        if (principal.accountId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return accountService.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private Account currentAccountOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.accountId() != null) {
                return accountService.findById(principal.accountId()).orElse(null);
            }
        }
        return null;
    }
}
