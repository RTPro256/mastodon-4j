# Key Domain Models

This document outlines the core domain models that need to be implemented for the Mastodon migration.

## Account

The Account entity represents a user account on the Mastodon network, either local or federated.

```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    private String domain; // null for local accounts
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    private String uri;
    private String url;
    private String avatarUrl;
    private String headerUrl;
    
    // ... additional fields
}
```

## Status

The Status entity represents a post (toot) in the Mastodon network.

```java
@Entity
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    private String uri;
    private String url;
    
    @Enumerated(EnumType.STRING)
    private Visibility visibility;
    
    // ... additional fields
}
```

## Additional Models to Implement

### User
Authentication credentials and user settings

### Follow
Relationship between accounts

### Favourite
Like/favorite relationship between accounts and statuses

### Notification
User notifications for various activities

### MediaAttachment
Images, videos, and other media files

### Application
OAuth applications that can access the API

### Tag
Hashtags used in statuses

### Mention
References to other accounts in statuses

### Poll
Polls attached to statuses

### List
User-created lists for organizing follows

### Filter
User-defined content filters

### Report
User reports for moderation
