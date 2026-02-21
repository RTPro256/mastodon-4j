package org.joinmastodon.web.api;

import java.util.List;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.service.SearchService;
import org.joinmastodon.core.service.StatusVisibilityService;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.SearchResultsDto;
import org.joinmastodon.web.api.dto.StatusDto;
import org.joinmastodon.web.api.dto.TagDto;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SearchController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final SearchService searchService;
    private final StatusVisibilityService statusVisibilityService;

    public SearchController(SearchService searchService, StatusVisibilityService statusVisibilityService) {
        this.searchService = searchService;
        this.statusVisibilityService = statusVisibilityService;
    }

    @GetMapping(ApiVersion.V2 + "/search")
    public SearchResultsDto searchV2(
            @RequestParam(value = "q") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        return doSearch(query, type, limit, offset);
    }

    @GetMapping(ApiVersion.V1 + "/search")
    public SearchResultsDto searchV1(
            @RequestParam(value = "q") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        return doSearch(query, type, limit, offset);
    }

    private SearchResultsDto doSearch(String query, String type, Integer limit, Integer offset) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(offset != null ? offset / resolvedLimit : 0, resolvedLimit);

        List<AccountDto> accounts = List.of();
        List<StatusDto> statuses = List.of();
        List<TagDto> hashtags = List.of();

        org.joinmastodon.core.entity.Account viewer = currentAccountOrNull();

        if (type == null || type.equalsIgnoreCase("accounts")) {
            accounts = searchService.searchAccounts(query, pageable).stream()
                    .limit(resolvedLimit)
                    .map(ApiMapper::toAccountDto)
                    .toList();
        }
        if (type == null || type.equalsIgnoreCase("statuses")) {
            statuses = searchService.searchStatuses(query, pageable).stream()
                    .filter(status -> statusVisibilityService.canView(status, viewer))
                    .map(ApiMapper::toStatusDto)
                    .toList();
        }
        if (type == null || type.equalsIgnoreCase("hashtags")) {
            hashtags = searchService.searchTags(query, pageable).stream()
                    .limit(resolvedLimit)
                    .map(SearchController::toTagDto)
                    .toList();
        }
        return new SearchResultsDto(accounts, statuses, hashtags);
    }

    private org.joinmastodon.core.entity.Account currentAccountOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            return null;
        }
        if (principal.accountId() == null) {
            return null;
        }
        // Return a minimal account for visibility checks
        org.joinmastodon.core.entity.Account account = new org.joinmastodon.core.entity.Account();
        account.setId(principal.accountId());
        return account;
    }

    private static TagDto toTagDto(Tag tag) {
        return new TagDto(tag.getName(), tag.getUrl());
    }
}
