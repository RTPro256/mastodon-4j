// mastodon-activitypub/src/main/java/org/joinmastodon/activitypub/model/Create.java
package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Create extends Activity {
    @JsonProperty("actor")
    private String actor;

    @JsonProperty("to")
    private List<String> to = new ArrayList<>();

    @JsonProperty("cc")
    private List<String> cc = new ArrayList<>();

    @JsonProperty("published")
    private Instant published;

    @JsonProperty("inReplyTo")
    private String inReplyTo;

    @JsonProperty("object")
    private Object object;

    // Getters and setters
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }

    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }

    public Instant getPublished() { return published; }
    public void setPublished(Instant published) { this.published = published; }

    public String getInReplyTo() { return inReplyTo; }
    public void setInReplyTo(String inReplyTo, Instant published) {
        this.inReplyTo = inReplyTo;
        this.published = published;
    }

    public Object getObject() { return object; }
    public void setObject(Object object) { this.object = object; }

    @Override
    public void validate() {
        // Ensure required fields are present
        if (actor == null || object == null) {
            throw new IllegalArgumentException("Missing required fields: actor and object");
        }
    }
}