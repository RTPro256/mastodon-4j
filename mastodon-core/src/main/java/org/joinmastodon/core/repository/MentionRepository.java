package org.joinmastodon.core.repository;

import org.joinmastodon.core.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionRepository extends JpaRepository<Mention, Long> {
}
