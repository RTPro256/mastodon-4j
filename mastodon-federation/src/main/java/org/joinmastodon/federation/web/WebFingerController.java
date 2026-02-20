package org.joinmastodon.federation.web;

import org.joinmastodon.federation.model.WebFingerResponse;
import org.joinmastodon.federation.service.WebFingerService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

/**
 * Controller for WebFinger protocol.
 * Handles discovery of ActivityPub actors via acct: URIs.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7033">RFC 7033</a>
 */
@RestController
@RequestMapping("/.well-known")
public class WebFingerController {

    private final WebFingerService webFingerService;

    public WebFingerController(WebFingerService webFingerService) {
        this.webFingerService = webFingerService;
    }

    /**
     * Handle WebFinger requests.
     * Returns a JRD (JSON Resource Descriptor) for the requested resource.
     *
     * @param resource the resource to resolve (e.g., acct:user@domain)
     * @return the WebFinger response or 404 if not found
     */
    @GetMapping(value = "/webfinger", produces = "application/jrd+json")
    public ResponseEntity<WebFingerResponse> webfinger(@RequestParam("resource") String resource) {
        Optional<WebFingerResponse> response = webFingerService.resolve(resource);
        
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/jrd+json"))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(response.get());
    }
}
