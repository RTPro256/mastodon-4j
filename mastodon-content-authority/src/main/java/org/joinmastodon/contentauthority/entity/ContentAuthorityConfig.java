package org.joinmastodon.contentauthority.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Configuration for the Content Authority (CA) functionality.
 */
@Entity
@Table(name = "content_authority_config")
public class ContentAuthorityConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false, unique = true)
    private String commonName;

    @Column(name = "organization")
    private String organization;

    @Column(name = "organizational_unit")
    private String organizationalUnit;

    @Column(name = "locality")
    private String locality;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "key_algorithm")
    private String keyAlgorithm = "RSA";

    @Column(name = "key_size")
    private Integer keySize = 4096;

    @Column(name = "signature_algorithm")
    private String signatureAlgorithm = "SHA512withRSA";

    @Column(name = "validity_years")
    private Integer validityYears = 10;

    @Column(name = "certificate_pem", columnDefinition = "TEXT")
    private String certificatePem;

    @Column(name = "private_key_encrypted", columnDefinition = "TEXT")
    private String privateKeyEncrypted;

    @Column(name = "auto_sign_content")
    private Boolean autoSignContent = false;

    @Column(name = "sign_statuses")
    private Boolean signStatuses = true;

    @Column(name = "sign_media")
    private Boolean signMedia = true;

    @Column(name = "sign_federated")
    private Boolean signFederated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public ContentAuthorityConfig() {}

    public ContentAuthorityConfig(String commonName) {
        this.commonName = commonName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(Integer validityYears) {
        this.validityYears = validityYears;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getPrivateKeyEncrypted() {
        return privateKeyEncrypted;
    }

    public void setPrivateKeyEncrypted(String privateKeyEncrypted) {
        this.privateKeyEncrypted = privateKeyEncrypted;
    }

    public Boolean getAutoSignContent() {
        return autoSignContent;
    }

    public void setAutoSignContent(Boolean autoSignContent) {
        this.autoSignContent = autoSignContent;
    }

    public Boolean getSignStatuses() {
        return signStatuses;
    }

    public void setSignStatuses(Boolean signStatuses) {
        this.signStatuses = signStatuses;
    }

    public Boolean getSignMedia() {
        return signMedia;
    }

    public void setSignMedia(Boolean signMedia) {
        this.signMedia = signMedia;
    }

    public Boolean getSignFederated() {
        return signFederated;
    }

    public void setSignFederated(Boolean signFederated) {
        this.signFederated = signFederated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if the CA is configured.
     */
    public boolean isConfigured() {
        return certificatePem != null && !certificatePem.isBlank();
    }

    /**
     * Check if the CA certificate is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
