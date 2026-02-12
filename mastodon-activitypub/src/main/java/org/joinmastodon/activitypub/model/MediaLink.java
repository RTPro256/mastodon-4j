package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaLink {
    private String type;
    private String mediaType;
    private String url;

    public MediaLink() {
    }

    public MediaLink(String type, String mediaType, String url) {
        this.type = type;
        this.mediaType = mediaType;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
