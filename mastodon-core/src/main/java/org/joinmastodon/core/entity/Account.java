package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(length = 255)
    private String domain;

    @Column(nullable = false, length = 255)
    private String acct;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private boolean bot = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "header_url", columnDefinition = "TEXT")
    private String headerUrl;

    @Column(name = "actor_uri", columnDefinition = "TEXT")
    private String actorUri;

    @Column(name = "inbox_url", columnDefinition = "TEXT")
    private String inboxUrl;

    @Column(name = "shared_inbox_url", columnDefinition = "TEXT")
    private String sharedInboxUrl;

    @Column(name = "public_key_pem", columnDefinition = "TEXT")
    private String publicKeyPem;

    @Column(name = "local_account", nullable = false)
    private boolean localAccount = true;

    @Column(name = "last_fetched_at")
    private Instant lastFetchedAt;

    @Column(name = "followers_count", nullable = false)
    private int followersCount = 0;

    @Column(name = "following_count", nullable = false)
    private int followingCount = 0;

    @Column(name = "statuses_count", nullable = false)
    private int statusesCount = 0;

    @Column(nullable = false)
    private boolean suspended = false;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    @Column(nullable = false)
    private boolean silenced = false;

    @Column(name = "silenced_at")
    private Instant silencedAt;

    @Column(nullable = false)
    private boolean disabled = false;

    @Column(name = "disabled_at")
    private Instant disabledAt;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAcct() {
        return acct;
    }

    public void setAcct(String acct) {
        this.acct = acct;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getHeaderUrl() {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl;
    }

    public String getActorUri() {
        return actorUri;
    }

    public void setActorUri(String actorUri) {
        this.actorUri = actorUri;
    }

    public String getInboxUrl() {
        return inboxUrl;
    }

    public void setInboxUrl(String inboxUrl) {
        this.inboxUrl = inboxUrl;
    }

    public String getSharedInboxUrl() {
        return sharedInboxUrl;
    }

    public void setSharedInboxUrl(String sharedInboxUrl) {
        this.sharedInboxUrl = sharedInboxUrl;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public boolean isLocalAccount() {
        return localAccount;
    }

    public void setLocalAccount(boolean localAccount) {
        this.localAccount = localAccount;
    }

    public Instant getLastFetchedAt() {
        return lastFetchedAt;
    }

    public void setLastFetchedAt(Instant lastFetchedAt) {
        this.lastFetchedAt = lastFetchedAt;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(int statusesCount) {
        this.statusesCount = statusesCount;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Instant getSuspendedAt() {
        return suspendedAt;
    }

    public void setSuspendedAt(Instant suspendedAt) {
        this.suspendedAt = suspendedAt;
    }

    public boolean isSilenced() {
        return silenced;
    }

    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    public Instant getSilencedAt() {
        return silencedAt;
    }

    public void setSilencedAt(Instant silencedAt) {
        this.silencedAt = silencedAt;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Instant getDisabledAt() {
        return disabledAt;
    }

    public void setDisabledAt(Instant disabledAt) {
        this.disabledAt = disabledAt;
    }
}
