package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAccount(Account account);
}
