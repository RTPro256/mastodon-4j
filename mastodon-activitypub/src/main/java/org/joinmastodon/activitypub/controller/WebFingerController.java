package org.joinmastodon.activitypub.controller;

import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.activitypub.model.WebFingerResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/well-known")
public class WebFingerController {

    @GetMapping("/webfinger")
    @GetMapping(".well-known/webfinger")
    public ResponseEntity<WebFingerResponse> getWebFinger(@RequestParam("resource") String resource) {
        // For simplicity, we'll assume the resource is the current user's identifier
        // In a real implementation, you would validate the resource and fetch the corresponding actor

        Actor actor = getActorFromResource(resource);

        WebFingerResponse response = new WebFingerResponse();
        response.setSubject(resource);

        // Add the self link
        response.getLinks().add("self");
        response.getLinks().add("http://webfinger.net/rel/activities");

        // Set HTTP caching headers
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3600, CacheControl.Unit.SECONDS).publicCache().sMaxAge(3600, CacheControl.Unit.SECONDS))
                .lastModified(Instant.now())
                .body(response);
    }

    private Actor getActorFromResource(String resource) {
        // In a real implementation, this would fetch the actor from the database
        // For now, we'll return a dummy actor
        return new Actor();
    }
}