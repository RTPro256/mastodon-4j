package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.repository.FavouriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavouriteService {
    private final FavouriteRepository favouriteRepository;

    public FavouriteService(FavouriteRepository favouriteRepository) {
        this.favouriteRepository = favouriteRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Favourite> findByAccountAndStatus(Account account, Status status) {
        return favouriteRepository.findByAccountAndStatus(account, status);
    }

    @Transactional(readOnly = true)
    public List<Favourite> findByAccount(Account account) {
        return favouriteRepository.findByAccount(account);
    }

    @Transactional(readOnly = true)
    public List<Favourite> findByStatus(Status status) {
        return favouriteRepository.findByStatusWithAccount(status);
    }

    @Transactional(readOnly = true)
    public List<Account> findAccountsByStatus(Status status) {
        return favouriteRepository.findByStatusWithAccount(status).stream()
                .map(Favourite::getAccount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Account> findAccountsByStatusId(Long statusId) {
        return favouriteRepository.findByStatusIdWithAccount(statusId).stream()
                .map(Favourite::getAccount)
                .toList();
    }

    @Transactional
    public Favourite save(Favourite favourite) {
        return favouriteRepository.save(favourite);
    }

    @Transactional
    public void delete(Favourite favourite) {
        favouriteRepository.delete(favourite);
    }

    @Transactional(readOnly = true)
    public long countByStatusId(Long statusId) {
        return favouriteRepository.countByStatusId(statusId);
    }
}
