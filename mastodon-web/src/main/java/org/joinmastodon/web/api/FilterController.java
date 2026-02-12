package org.joinmastodon.web.api;

import jakarta.validation.Valid;
import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Filter;
import org.joinmastodon.core.entity.FilterKeyword;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.FilterKeywordService;
import org.joinmastodon.core.service.FilterService;
import org.joinmastodon.web.api.dto.FilterDto;
import org.joinmastodon.web.api.dto.request.FilterCreateRequest;
import org.joinmastodon.web.api.dto.request.FilterKeywordRequest;
import org.joinmastodon.web.api.dto.request.FilterUpdateRequest;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping(ApiVersion.V2 + "/filters")
public class FilterController {
    private final FilterService filterService;
    private final FilterKeywordService filterKeywordService;
    private final AccountService accountService;

    public FilterController(FilterService filterService,
                            FilterKeywordService filterKeywordService,
                            AccountService accountService) {
        this.filterService = filterService;
        this.filterKeywordService = filterKeywordService;
        this.accountService = accountService;
    }

    @GetMapping
    public List<FilterDto> listFilters() {
        Account account = requireAccount();
        return filterService.findByAccount(account).stream()
                .map(ApiMapper::toFilterDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FilterDto getFilter(@PathVariable("id") String id) {
        Account account = requireAccount();
        Filter filter = filterService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter not found"));
        ensureOwner(filter, account);
        filter.setKeywords(filterKeywordService.findByFilter(filter));
        return ApiMapper.toFilterDto(filter);
    }

    @PostMapping
    public FilterDto create(@Valid @RequestBody FilterCreateRequest request) {
        Account account = requireAccount();
        Filter filter = new Filter();
        filter.setAccount(account);
        filter.setTitle(request.title());
        filter.setContext(ApiMapper.joinContext(request.context()));
        filter.setFilterAction(request.filterAction());
        filter.setExpiresAt(request.expiresAt());
        Filter saved = filterService.save(filter);
        List<FilterKeyword> keywords = saveKeywords(saved, request.keywords());
        saved.setKeywords(keywords);
        return ApiMapper.toFilterDto(saved);
    }

    @PutMapping("/{id}")
    public FilterDto update(@PathVariable("id") String id, @Valid @RequestBody FilterUpdateRequest request) {
        Account account = requireAccount();
        Filter filter = filterService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter not found"));
        ensureOwner(filter, account);
        if (request.title() != null) {
            filter.setTitle(request.title());
        }
        if (request.context() != null) {
            filter.setContext(ApiMapper.joinContext(request.context()));
        }
        if (request.filterAction() != null) {
            filter.setFilterAction(request.filterAction());
        }
        if (request.expiresAt() != null) {
            filter.setExpiresAt(request.expiresAt());
        }
        Filter saved = filterService.save(filter);
        if (request.keywords() != null) {
            filterKeywordService.findByFilter(saved).forEach(filterKeywordService::delete);
            List<FilterKeyword> keywords = saveKeywords(saved, request.keywords());
            saved.setKeywords(keywords);
        } else {
            saved.setKeywords(filterKeywordService.findByFilter(saved));
        }
        return ApiMapper.toFilterDto(saved);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        Account account = requireAccount();
        Filter filter = filterService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter not found"));
        ensureOwner(filter, account);
        filterKeywordService.findByFilter(filter).forEach(filterKeywordService::delete);
        filterService.delete(filter);
    }

    private List<FilterKeyword> saveKeywords(Filter filter, List<FilterKeywordRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(req -> {
            FilterKeyword keyword = new FilterKeyword();
            keyword.setFilter(filter);
            keyword.setKeyword(req.keyword());
            keyword.setWholeWord(req.wholeWord());
            return filterKeywordService.save(keyword);
        }).toList();
    }

    private void ensureOwner(Filter filter, Account account) {
        if (!filter.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
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
