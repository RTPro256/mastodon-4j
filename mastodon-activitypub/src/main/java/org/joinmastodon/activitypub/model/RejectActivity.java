package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Represents a Reject activity in ActivityPub.
 * Typically used to reject Follow requests.
 * @see <a href="https://www.w3.org/TR/activitypub/#reject-activity-inbox">Reject Activity</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RejectActivity {
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    private String id;
    private String type = "Reject";
    private String actor;
    private Object object;  // The Follow activity being rejected
    private Instant published;
    private List<String> to;

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

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
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
}