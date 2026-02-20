package org.joinmastodon.core.service;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.repository.AccountRepository;
import org.joinmastodon.core.repository.StatusRepository;
import org.joinmastodon.core.repository.TagRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final AccountRepository accountRepository;
    private final StatusRepository statusRepository;
    private final TagRepository tagRepository;

    public SearchService(
            AccountRepository accountRepository,
            StatusRepository statusRepository,
            TagRepository tagRepository) {
        this.accountRepository = accountRepository;
        this.statusRepository = statusRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Search accounts using PostgreSQL full-text search and trigram similarity.
     */
    @Transactional(readOnly = true)
    public List<Account> searchAccounts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        
        // Use trigram similarity for account search (better for partial matches)
        return accountRepository.findByUsernameContainingIgnoreCaseOrAcctContainingIgnoreCase(query, query);
    }

    /**
     * Search statuses using PostgreSQL full-text search.
     */
    @Transactional(readOnly = true)
    public List<Status> searchStatuses(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        
        // Use full-text search via tsvector
        return statusRepository.searchByContent(query, pageable);
    }

    /**
     * Search hashtags using pattern matching.
     */
    @Transactional(readOnly = true)
    public List<Tag> searchTags(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        
        // Remove leading # if present
        String cleanQuery = query.startsWith("#") ? query.substring(1) : query;
        return tagRepository.findByNameContainingIgnoreCase(cleanQuery);
    }

    /**
     * Unified search across all entity types.
     */
    @Transactional(readOnly = true)
    public SearchResults searchAll(String query, Pageable pageable) {
        List<Account> accounts = searchAccounts(query, pageable);
        List<Status> statuses = searchStatuses(query, pageable);
        List<Tag> tags = searchTags(query, pageable);
        
        return new SearchResults(accounts, statuses, tags);
    }

    public record SearchResults(
            List<Account> accounts,
            List<Status> statuses,
            List<Tag> tags) {}
}
