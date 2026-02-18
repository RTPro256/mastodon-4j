package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcceptActivity extends Activity {
    private Object object;

    public AcceptActivity(String actor, Object object) {
        super("Accept", actor);
        this.object = object;
    }

    @JsonProperty("object")
    public Object getObject() {
        return object;
    }

    @JsonProperty("object")
    public void setObject(Object object) {
        this.object = object;
    }
}