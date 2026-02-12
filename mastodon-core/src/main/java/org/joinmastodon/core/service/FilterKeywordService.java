package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Filter;
import org.joinmastodon.core.entity.FilterKeyword;
import org.joinmastodon.core.repository.FilterKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilterKeywordService {
    private final FilterKeywordRepository filterKeywordRepository;

    public FilterKeywordService(FilterKeywordRepository filterKeywordRepository) {
        this.filterKeywordRepository = filterKeywordRepository;
    }

    @Transactional(readOnly = true)
    public Optional<FilterKeyword> findById(Long id) {
        return filterKeywordRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<FilterKeyword> findByFilter(Filter filter) {
        return filterKeywordRepository.findByFilter(filter);
    }

    @Transactional
    public FilterKeyword save(FilterKeyword keyword) {
        return filterKeywordRepository.save(keyword);
    }

    @Transactional
    public void delete(FilterKeyword keyword) {
        filterKeywordRepository.delete(keyword);
    }
}
