package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "domain_blocks")
public class DomainBlock {
    
    public enum Severity {
        SILENCE,
        SUSPEND
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String domain;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.SILENCE;

    @Column(name = "reject_media", nullable = false)
    private boolean rejectMedia = false;

    @Column(name = "reject_reports", nullable = false)
    private boolean rejectReports = false;

    @Column(name = "private_comment", columnDefinition = "TEXT")
    private String privateComment;

    @Column(name = "public_comment", columnDefinition = "TEXT")
    private String publicComment;

    @Column(nullable = false)
    private boolean obfuscate = false;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public boolean isRejectMedia() {
        return rejectMedia;
    }

    public void setRejectMedia(boolean rejectMedia) {
        this.rejectMedia = rejectMedia;
    }

    public boolean isRejectReports() {
        return rejectReports;
    }

    public void setRejectReports(boolean rejectReports) {
        this.rejectReports = rejectReports;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public boolean isObfuscate() {
        return obfuscate;
    }

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
