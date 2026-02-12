package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Bookmark;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Bookmark> findByAccountAndStatus(Account account, Status status) {
        return bookmarkRepository.findByAccountAndStatus(account, status);
    }

    @Transactional(readOnly = true)
    public List<Bookmark> findByAccount(Account account) {
        return bookmarkRepository.findByAccount(account);
    }

    @Transactional
    public Bookmark save(Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void delete(Bookmark bookmark) {
        bookmarkRepository.delete(bookmark);
    }
}
