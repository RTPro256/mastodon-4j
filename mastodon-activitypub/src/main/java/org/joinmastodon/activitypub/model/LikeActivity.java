package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Represents a Like activity in ActivityPub.
 * Used for favoriting/liking objects (typically Notes/Statuses).
 * @see <a href="https://www.w3.org/TR/activitypub/#like-activity-inbox">Like Activity</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikeActivity {
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    private String id;
    private String type = "Like";
    private String actor;
    private String object;  // The URI of the object being liked
    private Instant published;
    private List<String> to;
    private List<String> cc;

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Instant getPublished() {
        return published;
    }

    public void setPublished(Instant published) {
        this.published = published;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }
}