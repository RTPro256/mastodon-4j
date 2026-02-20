package org.joinmastodon.activitypub.controller;

import org.joinmastodon.activitypub.model.Actor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/.well-known")
public class WebFingerController {

    @GetMapping("/webfinger")
    public ResponseEntity<WebFingerResponse> getWebFinger(@RequestParam("resource") String resource) {
        // For simplicity, we'll assume the resource is the current user's identifier
        // In a real implementation, you would validate the resource and fetch the corresponding actor

        Actor actor = getActorFromResource(resource);

        WebFingerResponse response = new WebFingerResponse();
        response.setSubject(resource);

        // Add the self link
        response.getLinks().add(new WebFingerLink("self", "application/activity+json", actor.getId()));

        // Set HTTP caching headers
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic())
                .lastModified(Instant.now())
                .body(response);
    }

    private Actor getActorFromResource(String resource) {
        // In a real implementation, this would fetch the actor from the database
        // For now, we'll return a dummy actor
        return new Actor();
    }

    // Simple WebFinger response class
    public static class WebFingerResponse {
        private String subject;
        private java.util.List<WebFingerLink> links = new java.util.ArrayList<>();

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public java.util.List<WebFingerLink> getLinks() { return links; }
        public void setLinks(java.util.List<WebFingerLink> links) { this.links = links; }
    }

    public static class WebFingerLink {
        private String rel;
        private String type;
        private String href;

        public WebFingerLink(String rel, String type, String href) {
            this.rel = rel;
            this.type = type;
            this.href = href;
        }

        public String getRel() { return rel; }
        public void setRel(String rel) { this.rel = rel; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }
}
