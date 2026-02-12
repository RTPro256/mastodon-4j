package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.ListAccount;
import org.joinmastodon.core.entity.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListAccountRepository extends JpaRepository<ListAccount, Long> {
    List<ListAccount> findByList(ListEntity list);
}
