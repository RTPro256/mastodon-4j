package org.joinmastodon.contentauthority.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A signature for a piece of content.
 */
@Entity
@Table(name = "content_signatures")
public class ContentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "signature_algorithm", nullable = false)
    private String signatureAlgorithm;

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signer_domain", nullable = false)
    private String signerDomain;

    @Column(name = "certificate_pem", columnDefinition = "TEXT")
    private String certificatePem;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.VALID;

    // Constructors
    public ContentSignature() {}

    public ContentSignature(String contentType, Long contentId, String contentHash,
                           String signatureAlgorithm, String signatureValue, String signerDomain) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.contentHash = contentHash;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = signatureValue;
        this.signerDomain = signerDomain;
        this.signedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getSignerDomain() {
        return signerDomain;
    }

    public void setSignerDomain(String signerDomain) {
        this.signerDomain = signerDomain;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    /**
     * Check if the signature is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if the signature is valid.
     */
    public boolean isValid() {
        return !revoked && !isExpired() && verificationStatus == VerificationStatus.VALID;
    }

    /**
     * Revoke the signature.
     */
    public void revoke(String reason) {
        this.revoked = true;
        this.revokedAt = Instant.now();
        this.revocationReason = reason;
        this.verificationStatus = VerificationStatus.REVOKED;
    }

    /**
     * Verification status.
     */
    public enum VerificationStatus {
        VALID,
        INVALID,
        EXPIRED,
        REVOKED,
        UNVERIFIED
    }
}
