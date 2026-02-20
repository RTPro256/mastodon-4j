package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "action_taken", nullable = false)
    private boolean actionTaken = false;

    @OneToMany(mappedBy = "report")
    private List<ReportStatus> statuses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "assigned_account_id")
    private Account assignedAccount;

    @Column(name = "action_taken_at")
    private Instant actionTakenAt;

    @ManyToOne
    @JoinColumn(name = "action_taken_by_account_id")
    private Account actionTakenBy;

    @Column(nullable = false)
    private boolean forwarded = false;

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

    public Account getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.targetAccount = targetAccount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(boolean actionTaken) {
        this.actionTaken = actionTaken;
    }

    public List<ReportStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ReportStatus> statuses) {
        this.statuses = statuses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Account getAssignedAccount() {
        return assignedAccount;
    }

    public void setAssignedAccount(Account assignedAccount) {
        this.assignedAccount = assignedAccount;
    }

    public Instant getActionTakenAt() {
        return actionTakenAt;
    }

    public void setActionTakenAt(Instant actionTakenAt) {
        this.actionTakenAt = actionTakenAt;
    }

    public Account getActionTakenBy() {
        return actionTakenBy;
    }

    public void setActionTakenBy(Account actionTakenBy) {
        this.actionTakenBy = actionTakenBy;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }
}
