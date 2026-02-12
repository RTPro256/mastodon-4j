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

    @Transactional
    public Favourite save(Favourite favourite) {
        return favouriteRepository.save(favourite);
    }

    @Transactional
    public void delete(Favourite favourite) {
        favouriteRepository.delete(favourite);
    }
}
