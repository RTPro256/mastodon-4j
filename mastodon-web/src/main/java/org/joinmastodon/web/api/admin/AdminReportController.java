package org.joinmastodon.web.api.admin;

import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.ModerationService;
import org.joinmastodon.core.service.ReportService;
import org.joinmastodon.web.api.ApiMapper;
import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.api.dto.AdminReportDto;
import org.joinmastodon.web.auth.AdminOnly;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/admin/reports")
@AdminOnly(moderator = true)
public class AdminReportController {

    private final ReportService reportService;
    private final AccountService accountService;
    private final ModerationService moderationService;

    public AdminReportController(ReportService reportService, AccountService accountService, ModerationService moderationService) {
        this.reportService = reportService;
        this.accountService = accountService;
        this.moderationService = moderationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminReportDto>> listReports(
            @RequestParam(value = "resolved", required = false) Boolean resolved,
            @RequestParam(value = "account_id", required = false) String accountId,
            @RequestParam(value = "target_account_id", required = false) String targetAccountId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        
        int resolvedLimit = limit == null ? 40 : Math.min(Math.max(limit, 1), 80);
        Pageable pageable = PageRequest.of(0, resolvedLimit, Sort.by("createdAt").descending());
        
        Page<Report> reports;
        if (resolved != null) {
            reports = reportService.findByActionTaken(resolved, pageable);
        } else if (targetAccountId != null) {
            Account targetAccount = accountService.findById(parseId(targetAccountId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));
            reports = reportService.findByTargetAccount(targetAccount, pageable);
        } else {
            reports = reportService.findAllOrderByCreatedAtDesc(pageable);
        }
        
        List<AdminReportDto> result = reports.getContent().stream()
                .map(this::toAdminReportDto)
                .toList();
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminReportDto> getReport(@PathVariable("id") String id) {
        Report report = reportService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        return ResponseEntity.ok(toAdminReportDto(report));
    }

    @PostMapping("/{id}/assign_to_self")
    public ResponseEntity<AdminReportDto> assignToSelf(@PathVariable("id") String id) {
        Report report = reportService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        
        Account currentAccount = getCurrentAccount();
        report = moderationService.assignReport(report, currentAccount);
        
        return ResponseEntity.ok(toAdminReportDto(report));
    }

    @PostMapping("/{id}/unassign")
    public ResponseEntity<AdminReportDto> unassign(@PathVariable("id") String id) {
        Report report = reportService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        
        report = moderationService.unassignReport(report);
        
        return ResponseEntity.ok(toAdminReportDto(report));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<AdminReportDto> resolve(@PathVariable("id") String id) {
        Report report = reportService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        
        Account currentAccount = getCurrentAccount();
        report = moderationService.resolveReport(report, currentAccount);
        
        return ResponseEntity.ok(toAdminReportDto(report));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<AdminReportDto> reopen(@PathVariable("id") String id) {
        Report report = reportService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        
        report = moderationService.reopenReport(report);
        
        return ResponseEntity.ok(toAdminReportDto(report));
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return accountService.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    private AdminReportDto toAdminReportDto(Report report) {
        return new AdminReportDto(
                String.valueOf(report.getId()),
                report.isActionTaken(),
                report.getActionTakenAt(),
                "other", // category
                report.getComment(),
                report.isForwarded(),
                report.getCreatedAt(),
                report.getCreatedAt(), // updated_at
                report.getAccount() != null ? ApiMapper.toAccountDto(report.getAccount()) : null,
                report.getTargetAccount() != null ? ApiMapper.toAccountDto(report.getTargetAccount()) : null,
                report.getAssignedAccount() != null ? ApiMapper.toAccountDto(report.getAssignedAccount()) : null,
                report.getActionTakenBy() != null ? ApiMapper.toAccountDto(report.getActionTakenBy()) : null,
                List.of(), // statuses
                List.of()  // rules
        );
    }
}
