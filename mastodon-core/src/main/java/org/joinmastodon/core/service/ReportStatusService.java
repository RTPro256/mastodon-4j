package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.entity.ReportStatus;
import org.joinmastodon.core.repository.ReportStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportStatusService {
    private final ReportStatusRepository reportStatusRepository;

    public ReportStatusService(ReportStatusRepository reportStatusRepository) {
        this.reportStatusRepository = reportStatusRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ReportStatus> findById(Long id) {
        return reportStatusRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ReportStatus> findByReport(Report report) {
        return reportStatusRepository.findByReport(report);
    }

    @Transactional
    public ReportStatus save(ReportStatus reportStatus) {
        return reportStatusRepository.save(reportStatus);
    }

    @Transactional
    public void delete(ReportStatus reportStatus) {
        reportStatusRepository.delete(reportStatus);
    }
}
