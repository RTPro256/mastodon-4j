package org.joinmastodon.core.service;

import java.time.Duration;
import java.time.Instant;
import org.joinmastodon.core.entity.RateLimitEntry;
import org.joinmastodon.core.repository.RateLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateLimitService {
    private final RateLimitRepository rateLimitRepository;

    public RateLimitService(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }

    @Transactional
    public boolean allow(String key, Instant now, int limit, Duration window) {
        RateLimitEntry entry = rateLimitRepository.findById(key).orElse(null);
        if (entry == null) {
            RateLimitEntry created = new RateLimitEntry();
            created.setKey(key);
            created.setWindowStart(now);
            created.setCount(1);
            rateLimitRepository.save(created);
            return true;
        }

        Instant windowEnd = entry.getWindowStart().plus(window);
        if (now.isAfter(windowEnd)) {
            entry.setWindowStart(now);
            entry.setCount(1);
            rateLimitRepository.save(entry);
            return true;
        }

        if (entry.getCount() >= limit) {
            return false;
        }

        entry.setCount(entry.getCount() + 1);
        rateLimitRepository.save(entry);
        return true;
    }
}
