package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportStatusRepository extends JpaRepository<ReportStatus, Long> {
    List<ReportStatus> findByReport(Report report);
}
