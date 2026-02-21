// mastodon-activitypub/src/main/java/org/joinmastodon/activitypub/model/Activity.java
package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Activity {
    // ActivityPub context - instance field for proper serialization
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    // Core fields
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("actor")
    private String actor;

    @JsonProperty("object")
    private Object object;

    @JsonProperty("to")
    private List<String> to = new ArrayList<>();

    @JsonProperty("cc")
    private List<String> cc = new ArrayList<>();

    @JsonProperty("published")
    private Instant published;

    @JsonProperty("inReplyTo")
    private String inReplyTo;

    // Default constructor
    protected Activity() {
    }

    // Constructor with type and actor
    protected Activity(String type, String actor) {
        this.type = type;
        this.actor = actor;
    }

    // Getters and setters
    public Object getContext() { return context; }
    public void setContext(Object context) { this.context = context; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public Object getObject() { return object; }
    public void setObject(Object object) { this.object = object; }

    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }

    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }

    public Instant getPublished() { return published; }
    public void setPublished(Instant published) { this.published = published; }

    public String getInReplyTo() { return inReplyTo; }
    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    // Validation method for subclasses to implement
    public void validate() {
        // Default implementation - subclasses can override
    }
}