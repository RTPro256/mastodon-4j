package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Session;
import org.joinmastodon.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByUser(User user);
}
