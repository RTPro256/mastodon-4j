package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderedCollection<T> {
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    private String id;
    private String type = "OrderedCollection";
    private int totalItems;
    private List<T> orderedItems;

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

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<T> getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(List<T> orderedItems) {
        this.orderedItems = orderedItems;
    }
}
