package org.joinmastodon.web.api;

import java.util.List;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.core.service.TagService;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.SearchResultsDto;
import org.joinmastodon.web.api.dto.StatusDto;
import org.joinmastodon.web.api.dto.TagDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V2 + "/search")
public class SearchController {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    private final AccountService accountService;
    private final StatusService statusService;
    private final TagService tagService;

    public SearchController(AccountService accountService, StatusService statusService, TagService tagService) {
        this.accountService = accountService;
        this.statusService = statusService;
        this.tagService = tagService;
    }

    @GetMapping
    public SearchResultsDto search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false) Integer limit) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        Pageable pageable = PageRequest.of(0, resolvedLimit);

        List<AccountDto> accounts = List.of();
        List<StatusDto> statuses = List.of();
        List<TagDto> hashtags = List.of();

        if (type == null || type.equalsIgnoreCase("accounts")) {
            accounts = accountService.searchByUsernameOrAcct(query).stream()
                    .limit(resolvedLimit)
                    .map(ApiMapper::toAccountDto)
                    .toList();
        }
        if (type == null || type.equalsIgnoreCase("statuses")) {
            statuses = statusService.searchByContent(query, pageable).stream()
                    .filter(status -> status.getVisibility() == Visibility.PUBLIC
                            || status.getVisibility() == Visibility.UNLISTED)
                    .map(ApiMapper::toStatusDto)
                    .toList();
        }
        if (type == null || type.equalsIgnoreCase("hashtags")) {
            hashtags = tagService.searchByName(query).stream()
                    .limit(resolvedLimit)
                    .map(SearchController::toTagDto)
                    .toList();
        }
        return new SearchResultsDto(accounts, statuses, hashtags);
    }

    private static TagDto toTagDto(Tag tag) {
        return new TagDto(tag.getName(), tag.getUrl());
    }
}
