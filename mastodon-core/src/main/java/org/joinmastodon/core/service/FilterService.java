package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Filter;
import org.joinmastodon.core.repository.FilterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilterService {
    private final FilterRepository filterRepository;

    public FilterService(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Filter> findById(Long id) {
        return filterRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Filter> findByAccount(Account account) {
        return filterRepository.findByAccount(account);
    }

    @Transactional
    public Filter save(Filter filter) {
        return filterRepository.save(filter);
    }

    @Transactional
    public void delete(Filter filter) {
        filterRepository.delete(filter);
    }
}
