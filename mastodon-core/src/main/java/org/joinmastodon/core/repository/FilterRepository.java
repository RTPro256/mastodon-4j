package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    List<Filter> findByAccount(Account account);
}
