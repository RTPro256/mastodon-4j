package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Filter;
import org.joinmastodon.core.entity.FilterKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterKeywordRepository extends JpaRepository<FilterKeyword, Long> {
    List<FilterKeyword> findByFilter(Filter filter);
}
