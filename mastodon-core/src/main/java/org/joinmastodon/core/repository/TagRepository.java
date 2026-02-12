package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
