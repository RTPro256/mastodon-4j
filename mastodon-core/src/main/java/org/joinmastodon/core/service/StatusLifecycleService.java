package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Bookmark;
import org.joinmastodon.core.entity.Favourite;
import org.joinmastodon.core.entity.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusLifecycleService {
    private final StatusService statusService;
    private final FavouriteService favouriteService;
    private final BookmarkService bookmarkService;
    private final AccountService accountService;

    public StatusLifecycleService(StatusService statusService,
                                  FavouriteService favouriteService,
                                  BookmarkService bookmarkService,
                                  AccountService accountService) {
        this.statusService = statusService;
        this.favouriteService = favouriteService;
        this.bookmarkService = bookmarkService;
        this.accountService = accountService;
    }

    @Transactional
    public Status create(Status status) {
        Status saved = statusService.save(status);
        Account account = saved.getAccount();
        if (account != null) {
            account.setStatusesCount(account.getStatusesCount() + 1);
            accountService.save(account);
        }
        return saved;
    }

    @Transactional
    public Status reblog(Account account, Status target) {
        Optional<Status> existing = statusService.findByAccountAndReblog(account, target);
        if (existing.isPresent()) {
            return existing.get();
        }
        Status reblog = new Status();
        reblog.setAccount(account);
        reblog.setReblog(target);
        reblog.setContent("");
        reblog.setVisibility(target.getVisibility());
        Status saved = statusService.save(reblog);
        if (account != null) {
            account.setStatusesCount(account.getStatusesCount() + 1);
            accountService.save(account);
        }
        return saved;
    }

    @Transactional
    public void delete(Status status) {
        statusService.delete(status);
        Account account = status.getAccount();
        if (account != null) {
            account.setStatusesCount(Math.max(0, account.getStatusesCount() - 1));
            accountService.save(account);
        }
    }

    @Transactional
    public void favourite(Account account, Status status) {
        favouriteService.findByAccountAndStatus(account, status).orElseGet(() -> {
            Favourite favourite = new Favourite();
            favourite.setAccount(account);
            favourite.setStatus(status);
            return favouriteService.save(favourite);
        });
    }

    @Transactional
    public void unfavourite(Account account, Status status) {
        favouriteService.findByAccountAndStatus(account, status).ifPresent(favouriteService::delete);
    }

    @Transactional
    public void bookmark(Account account, Status status) {
        bookmarkService.findByAccountAndStatus(account, status).orElseGet(() -> {
            Bookmark bookmark = new Bookmark();
            bookmark.setAccount(account);
            bookmark.setStatus(status);
            return bookmarkService.save(bookmark);
        });
    }

    @Transactional
    public void unbookmark(Account account, Status status) {
        bookmarkService.findByAccountAndStatus(account, status).ifPresent(bookmarkService::delete);
    }
}
