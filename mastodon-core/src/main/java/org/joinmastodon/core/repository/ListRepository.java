package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListRepository extends JpaRepository<ListEntity, Long> {
    List<ListEntity> findByAccount(Account account);
}
