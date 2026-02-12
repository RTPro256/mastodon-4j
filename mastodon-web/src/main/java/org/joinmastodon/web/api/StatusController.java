package org.joinmastodon.web.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Bookmark;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.BookmarkService;
import org.joinmastodon.core.service.FavouriteService;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.core.service.StatusVisibilityService;
import org.joinmastodon.web.streaming.StreamingNotifier;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.joinmastodon.web.api.dto.StatusContextDto;
import org.joinmastodon.web.api.dto.StatusDto;
import org.joinmastodon.web.api.dto.request.StatusCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/statuses")
public class StatusController {
    private final StatusService statusService;
    private final FavouriteService favouriteService;
    private final BookmarkService bookmarkService;
    private final AccountService accountService;
    private final MediaAttachmentService mediaAttachmentService;
    private final StatusVisibilityService statusVisibilityService;
    private final StreamingNotifier streamingNotifier;

    public StatusController(StatusService statusService,
                            FavouriteService favouriteService,
                            BookmarkService bookmarkService,
                            AccountService accountService,
                            MediaAttachmentService mediaAttachmentService,
                            StatusVisibilityService statusVisibilityService,
                            StreamingNotifier streamingNotifier) {
        this.statusService = statusService;
        this.favouriteService = favouriteService;
        this.bookmarkService = bookmarkService;
        this.accountService = accountService;
        this.mediaAttachmentService = mediaAttachmentService;
        this.statusVisibilityService = statusVisibilityService;
        this.streamingNotifier = streamingNotifier;
    }

    @GetMapping("/{id}")
    public StatusDto getStatus(@PathVariable("id") String id) {
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        if (!statusVisibilityService.canView(status, currentAccountOrNull())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found");
        }
        return ApiMapper.toStatusDto(status);
    }

    @PostMapping
    public StatusDto createStatus(@Valid @RequestBody StatusCreateRequest request) {
        Account account = requireAccount();
        Status status = new Status();
        status.setAccount(account);
        status.setContent(request.status());
        status.setVisibility(resolveVisibility(request.visibility()));
        status.setSensitive(Boolean.TRUE.equals(request.sensitive()));
        status.setSpoilerText(request.spoilerText());
        status.setLanguage(request.language());
        Long inReplyToId = parseOptionalId(request.inReplyToId());
        status.setInReplyToId(inReplyToId);
        if (inReplyToId != null) {
            statusService.findById(inReplyToId)
                    .ifPresent(parent -> status.setInReplyToAccountId(parent.getAccount().getId()));
        }
        if (request.mediaIds() != null && !request.mediaIds().isEmpty()) {
            List<Long> mediaIds = request.mediaIds().stream()
                    .map(this::parseOptionalId)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            List<MediaAttachment> attachments = mediaIds.stream()
                    .map(mediaId -> mediaAttachmentService.findById(mediaId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media not found")))
                    .filter(media -> media.getAccountId().equals(account.getId()))
                    .toList();
            if (attachments.size() != mediaIds.size()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid media ownership");
            }
            status.setMediaAttachments(attachments);
        }
        Status saved = statusService.save(status);
        account.setStatusesCount(account.getStatusesCount() + 1);
        accountService.save(account);
        streamingNotifier.notifyStatus(saved);
        return ApiMapper.toStatusDto(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        if (!status.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        statusService.delete(status);
        Account owner = status.getAccount();
        owner.setStatusesCount(Math.max(0, owner.getStatusesCount() - 1));
        accountService.save(owner);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/context")
    public StatusContextDto getContext(@PathVariable("id") String id) {
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        Account viewer = currentAccountOrNull();
        if (!statusVisibilityService.canView(status, viewer)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found");
        }

        List<StatusDto> ancestors = buildAncestors(status, viewer);
        List<StatusDto> descendants = statusService.findReplies(status.getId()).stream()
                .filter(reply -> statusVisibilityService.canView(reply, viewer))
                .map(ApiMapper::toStatusDto)
                .toList();
        return new StatusContextDto(ancestors, descendants);
    }

    @PostMapping("/{id}/favourite")
    public StatusDto favourite(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        favouriteService.findByAccountAndStatus(account, status).orElseGet(() -> {
            Favourite favourite = new Favourite();
            favourite.setAccount(account);
            favourite.setStatus(status);
            return favouriteService.save(favourite);
        });
        return ApiMapper.toStatusDto(status);
    }

    @PostMapping("/{id}/unfavourite")
    public StatusDto unfavourite(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        favouriteService.findByAccountAndStatus(account, status)
                .ifPresent(favouriteService::delete);
        return ApiMapper.toStatusDto(status);
    }

    @PostMapping("/{id}/bookmark")
    public StatusDto bookmark(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        bookmarkService.findByAccountAndStatus(account, status).orElseGet(() -> {
            Bookmark bookmark = new Bookmark();
            bookmark.setAccount(account);
            bookmark.setStatus(status);
            return bookmarkService.save(bookmark);
        });
        return ApiMapper.toStatusDto(status);
    }

    @PostMapping("/{id}/unbookmark")
    public StatusDto unbookmark(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        bookmarkService.findByAccountAndStatus(account, status)
                .ifPresent(bookmarkService::delete);
        return ApiMapper.toStatusDto(status);
    }

    @PostMapping("/{id}/reblog")
    public StatusDto reblog(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status target = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        Optional<Status> existing = statusService.findByAccountAndReblog(account, target);
        if (existing.isPresent()) {
            return ApiMapper.toStatusDto(existing.get());
        }
        Status reblog = new Status();
        reblog.setAccount(account);
        reblog.setReblog(target);
        reblog.setContent("");
        reblog.setVisibility(target.getVisibility());
        Status saved = statusService.save(reblog);
        account.setStatusesCount(account.getStatusesCount() + 1);
        accountService.save(account);
        streamingNotifier.notifyStatus(saved);
        return ApiMapper.toStatusDto(saved);
    }

    @PostMapping("/{id}/unreblog")
    public StatusDto unreblog(@PathVariable("id") String id) {
        Account account = requireAccount();
        Status target = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        statusService.findByAccountAndReblog(account, target)
                .ifPresent(reblog -> {
                    statusService.delete(reblog);
                    account.setStatusesCount(Math.max(0, account.getStatusesCount() - 1));
                    accountService.save(account);
                });
        return ApiMapper.toStatusDto(target);
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

    private Visibility resolveVisibility(String value) {
        Visibility visibility = Visibility.fromApiValue(value);
        return visibility == null ? Visibility.PUBLIC : visibility;
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

    private Account currentAccountOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.accountId() != null) {
                return accountService.findById(principal.accountId()).orElse(null);
            }
        }
        return null;
    }

    private List<StatusDto> buildAncestors(Status status, Account viewer) {
        List<StatusDto> ancestors = new java.util.ArrayList<>();
        Long parentId = status.getInReplyToId();
        int guard = 0;
        while (parentId != null && guard < 20) {
            Status parent = statusService.findById(parentId).orElse(null);
            if (parent == null) {
                break;
            }
            if (statusVisibilityService.canView(parent, viewer)) {
                ancestors.add(0, ApiMapper.toStatusDto(parent));
            }
            parentId = parent.getInReplyToId();
            guard++;
        }
        return ancestors;
    }
}
