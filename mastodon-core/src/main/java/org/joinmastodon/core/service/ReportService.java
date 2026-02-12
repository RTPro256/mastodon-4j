package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Report> findById(Long id) {
        return reportRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Report> findByAccount(Account account) {
        return reportRepository.findByAccount(account);
    }

    @Transactional
    public Report save(Report report) {
        return reportRepository.save(report);
    }

    @Transactional
    public void delete(Report report) {
        reportRepository.delete(report);
    }
}
