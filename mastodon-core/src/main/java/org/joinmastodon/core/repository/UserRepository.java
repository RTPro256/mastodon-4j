package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAccount(Account account);
}
