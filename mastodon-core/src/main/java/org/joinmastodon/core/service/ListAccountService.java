package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.ListAccount;
import org.joinmastodon.core.entity.ListEntity;
import org.joinmastodon.core.repository.ListAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAccountService {
    private final ListAccountRepository listAccountRepository;

    public ListAccountService(ListAccountRepository listAccountRepository) {
        this.listAccountRepository = listAccountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ListAccount> findById(Long id) {
        return listAccountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ListAccount> findByList(ListEntity list) {
        return listAccountRepository.findByList(list);
    }

    @Transactional
    public ListAccount save(ListAccount listAccount) {
        return listAccountRepository.save(listAccount);
    }

    @Transactional
    public void delete(ListAccount listAccount) {
        listAccountRepository.delete(listAccount);
    }
}
