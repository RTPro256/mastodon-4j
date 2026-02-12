package org.joinmastodon.core.repository;

import org.joinmastodon.core.entity.RateLimitEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimitRepository extends JpaRepository<RateLimitEntry, String> {
}
