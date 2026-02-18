package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Represents a Tombstone object in ActivityPub.
 * Used as a placeholder for deleted objects.
 * @see <a href="https://www.w3.org/TR/activitystreams-vocabulary/#dfn-tombstone">Tombstone</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tombstone {
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    private String id;
    private String type = "Tombstone";
    private Instant deleted;
    private String formerType;  // The type of the object that was deleted

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

    public Instant getDeleted() {
        return deleted;
    }

    public void setDeleted(Instant deleted) {
        this.deleted = deleted;
    }

    public String getFormerType() {
        return formerType;
    }

    public void setFormerType(String formerType) {
        this.formerType = formerType;
    }
}