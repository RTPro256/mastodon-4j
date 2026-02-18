package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a paginated OrderedCollectionPage in ActivityPub.
 * Used for paginating through large collections like followers, following, outbox.
 * @see <a href="https://www.w3.org/TR/activitystreams-core/#paging">Activity Streams Paging</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderedCollectionPage<T> {
    @JsonProperty("@context")
    private Object context = ActivityPubContext.DEFAULT;

    private String id;
    private String type = "OrderedCollectionPage";
    private int totalItems;
    private List<T> orderedItems;
    private String first;
    private String next;
    private String prev;
    private String last;
    private String partOf;  // The parent collection

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

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getPartOf() {
        return partOf;
    }

    public void setPartOf(String partOf) {
        this.partOf = partOf;
    }
}