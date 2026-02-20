package org.joinmastodon.core.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.AccountAction;
import org.joinmastodon.core.entity.DomainBlock;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.repository.AccountActionRepository;
import org.joinmastodon.core.repository.AccountRepository;
import org.joinmastodon.core.repository.DomainBlockRepository;
import org.joinmastodon.core.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModerationService {
    
    private final AccountRepository accountRepository;
    private final AccountActionRepository accountActionRepository;
    private final DomainBlockRepository domainBlockRepository;
    private final ReportRepository reportRepository;

    public ModerationService(
            AccountRepository accountRepository,
            AccountActionRepository accountActionRepository,
            DomainBlockRepository domainBlockRepository,
            ReportRepository reportRepository) {
        this.accountRepository = accountRepository;
        this.accountActionRepository = accountActionRepository;
        this.domainBlockRepository = domainBlockRepository;
        this.reportRepository = reportRepository;
    }

    // Account moderation actions
    
    @Transactional
    public AccountAction suspendAccount(Account targetAccount, Account actionBy, String reason, Report report) {
        targetAccount.setSuspended(true);
        targetAccount.setSuspendedAt(Instant.now());
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.SUSPEND, reason, report);
    }

    @Transactional
    public AccountAction unsuspendAccount(Account targetAccount, Account actionBy, String reason) {
        targetAccount.setSuspended(false);
        targetAccount.setSuspendedAt(null);
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.UNSUSPEND, reason, null);
    }

    @Transactional
    public AccountAction silenceAccount(Account targetAccount, Account actionBy, String reason, Report report) {
        targetAccount.setSilenced(true);
        targetAccount.setSilencedAt(Instant.now());
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.SILENCE, reason, report);
    }

    @Transactional
    public AccountAction unsilenceAccount(Account targetAccount, Account actionBy, String reason) {
        targetAccount.setSilenced(false);
        targetAccount.setSilencedAt(null);
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.UNSILENCE, reason, null);
    }

    @Transactional
    public AccountAction disableAccount(Account targetAccount, Account actionBy, String reason, Report report) {
        targetAccount.setDisabled(true);
        targetAccount.setDisabledAt(Instant.now());
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.DISABLE, reason, report);
    }

    @Transactional
    public AccountAction enableAccount(Account targetAccount, Account actionBy, String reason) {
        targetAccount.setDisabled(false);
        targetAccount.setDisabledAt(null);
        accountRepository.save(targetAccount);
        
        return recordAction(targetAccount, actionBy, AccountAction.ActionType.ENABLE, reason, null);
    }

    private AccountAction recordAction(Account targetAccount, Account actionBy, 
            AccountAction.ActionType actionType, String reason, Report report) {
        AccountAction action = new AccountAction();
        action.setTargetAccount(targetAccount);
        action.setActionTakenBy(actionBy);
        action.setAccount(actionBy); // The account that took the action
        action.setActionType(actionType);
        action.setReason(reason);
        action.setReport(report);
        return accountActionRepository.save(action);
    }

    public List<AccountAction> getAccountActions(Account targetAccount) {
        return accountActionRepository.findByTargetAccount(targetAccount);
    }

    public Page<AccountAction> getAccountActions(Account targetAccount, Pageable pageable) {
        return accountActionRepository.findByTargetAccountOrderByCreatedAtDesc(targetAccount, pageable);
    }

    // Domain blocks
    
    @Transactional
    public DomainBlock blockDomain(String domain, DomainBlock.Severity severity, 
            boolean rejectMedia, boolean rejectReports, String privateComment, 
            String publicComment, boolean obfuscate) {
        if (domainBlockRepository.existsByDomain(domain)) {
            throw new IllegalArgumentException("Domain already blocked: " + domain);
        }
        
        DomainBlock block = new DomainBlock();
        block.setDomain(domain);
        block.setSeverity(severity);
        block.setRejectMedia(rejectMedia);
        block.setRejectReports(rejectReports);
        block.setPrivateComment(privateComment);
        block.setPublicComment(publicComment);
        block.setObfuscate(obfuscate);
        
        return domainBlockRepository.save(block);
    }

    @Transactional
    public void unblockDomain(Long domainBlockId) {
        domainBlockRepository.deleteById(domainBlockId);
    }

    public Optional<DomainBlock> getDomainBlock(Long id) {
        return domainBlockRepository.findById(id);
    }

    public Optional<DomainBlock> getDomainBlockByDomain(String domain) {
        return domainBlockRepository.findByDomain(domain);
    }

    public Page<DomainBlock> getDomainBlocks(Pageable pageable) {
        return domainBlockRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public boolean isDomainBlocked(String domain) {
        if (domain == null) {
            return false;
        }
        return domainBlockRepository.findByDomain(domain).isPresent();
    }

    // Report management
    
    @Transactional
    public Report assignReport(Report report, Account assignedTo) {
        report.setAssignedAccount(assignedTo);
        return reportRepository.save(report);
    }

    @Transactional
    public Report unassignReport(Report report) {
        report.setAssignedAccount(null);
        return reportRepository.save(report);
    }

    @Transactional
    public Report resolveReport(Report report, Account resolvedBy) {
        report.setActionTaken(true);
        report.setActionTakenAt(Instant.now());
        report.setActionTakenBy(resolvedBy);
        return reportRepository.save(report);
    }

    @Transactional
    public Report reopenReport(Report report) {
        report.setActionTaken(false);
        report.setActionTakenAt(null);
        report.setActionTakenBy(null);
        return reportRepository.save(report);
    }
}
