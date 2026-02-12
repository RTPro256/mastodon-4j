package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.ListEntity;
import org.joinmastodon.core.repository.ListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListService {
    private final ListRepository listRepository;

    public ListService(ListRepository listRepository) {
        this.listRepository = listRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ListEntity> findById(Long id) {
        return listRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ListEntity> findByAccount(Account account) {
        return listRepository.findByAccount(account);
    }

    @Transactional
    public ListEntity save(ListEntity list) {
        return listRepository.save(list);
    }

    @Transactional
    public void delete(ListEntity list) {
        listRepository.delete(list);
    }
}
