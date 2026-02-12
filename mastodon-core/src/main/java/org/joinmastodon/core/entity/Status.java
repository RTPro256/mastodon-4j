package org.joinmastodon.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.joinmastodon.core.model.Visibility;

@Entity
@Table(name = "statuses", indexes = {
        @Index(name = "idx_statuses_account_id", columnList = "account_id"),
        @Index(name = "idx_statuses_created_at", columnList = "created_at"),
        @Index(name = "idx_statuses_in_reply_to_id", columnList = "in_reply_to_id")
})
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "in_reply_to_id")
    private Long inReplyToId;

    @Column(name = "in_reply_to_account_id")
    private Long inReplyToAccountId;

    @Column(nullable = false)
    private boolean sensitive = false;

    @Column(name = "spoiler_text", columnDefinition = "TEXT")
    private String spoilerText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(length = 10)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String uri;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "reblog_of_id")
    private Status reblog;

    @ManyToMany
    @JoinTable(name = "status_media_attachments",
            joinColumns = @JoinColumn(name = "status_id"),
            inverseJoinColumns = @JoinColumn(name = "media_attachment_id"))
    @OrderColumn(name = "position")
    private List<MediaAttachment> mediaAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "status")
    private List<Mention> mentions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "statuses_tags",
            joinColumns = @JoinColumn(name = "status_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @OneToOne(mappedBy = "status")
    private Poll poll;

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getInReplyToId() {
        return inReplyToId;
    }

    public void setInReplyToId(Long inReplyToId) {
        this.inReplyToId = inReplyToId;
    }

    public Long getInReplyToAccountId() {
        return inReplyToAccountId;
    }

    public void setInReplyToAccountId(Long inReplyToAccountId) {
        this.inReplyToAccountId = inReplyToAccountId;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getSpoilerText() {
        return spoilerText;
    }

    public void setSpoilerText(String spoilerText) {
        this.spoilerText = spoilerText;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Status getReblog() {
        return reblog;
    }

    public void setReblog(Status reblog) {
        this.reblog = reblog;
    }

    public List<MediaAttachment> getMediaAttachments() {
        return mediaAttachments;
    }

    public void setMediaAttachments(List<MediaAttachment> mediaAttachments) {
        this.mediaAttachments = mediaAttachments;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public void setMentions(List<Mention> mentions) {
        this.mentions = mentions;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
