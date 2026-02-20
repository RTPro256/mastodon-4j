package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "account_actions")
public class AccountAction {
    
    public enum ActionType {
        SUSPEND,
        SILENCE,
        DISABLE,
        UNSUSPEND,
        UNSILENCE,
        ENABLE
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "action_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne(optional = false)
    @JoinColumn(name = "action_taken_by_account_id", nullable = false)
    private Account actionTakenBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Account getActionTakenBy() {
        return actionTakenBy;
    }

    public void setActionTakenBy(Account actionTakenBy) {
        this.actionTakenBy = actionTakenBy;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.targetAccount = targetAccount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
