package org.joinmastodon.federation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebFingerResponse {
    private String subject;
    private List<String> aliases;
    private List<WebFingerLink> links;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<WebFingerLink> getLinks() {
        return links;
    }

    public void setLinks(List<WebFingerLink> links) {
        this.links = links;
    }
}
