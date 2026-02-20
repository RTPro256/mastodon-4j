package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.entity.ReportNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportNoteRepository extends JpaRepository<ReportNote, Long> {
    
    List<ReportNote> findByReportOrderByCreatedAtDesc(Report report);
    
    void deleteByReportId(Long reportId);
}
