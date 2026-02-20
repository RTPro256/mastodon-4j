package org.joinmastodon.core.repository;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAccount(Account account);
    
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Report> findByActionTakenOrderByCreatedAtDesc(boolean actionTaken, Pageable pageable);
    
    Page<Report> findByAssignedAccountOrderByCreatedAtDesc(Account assignedAccount, Pageable pageable);
    
    Page<Report> findByTargetAccountOrderByCreatedAtDesc(Account targetAccount, Pageable pageable);
    
    long countByActionTaken(boolean actionTaken);
}
