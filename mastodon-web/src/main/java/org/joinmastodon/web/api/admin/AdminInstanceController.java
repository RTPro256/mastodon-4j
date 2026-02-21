package org.joinmastodon.web.api.admin;

import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.auth.AdminOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/admin")
@AdminOnly
public class AdminInstanceController {

    @Value("${mastodon.instance.title:Mastodon Java}")
    private String instanceTitle;

    @Value("${mastodon.instance.description:A Mastodon instance running on Java}")
    private String instanceDescription;

    @Value("${mastodon.instance.contact-email:admin@localhost}")
    private String contactEmail;

    @Value("${mastodon.instance.source-url:https://github.com/mastodon/mastodon}")
    private String sourceUrl;

    // In-memory storage for instance settings (in production, this would be in the database)
    private String thumbnailUrl = "";
    private String shortDescription = "";
    private String terms = "";
    private boolean registrationsOpen = true;
    private boolean approvalsRequired = false;

    @GetMapping("/instance")
    public ResponseEntity<AdminInstanceDto> getInstance() {
        AdminInstanceDto dto = new AdminInstanceDto(
                instanceTitle,
                instanceDescription,
                shortDescription,
                contactEmail,
                thumbnailUrl,
                sourceUrl,
                terms,
                registrationsOpen,
                approvalsRequired
        );
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/instance")
    public ResponseEntity<AdminInstanceDto> updateInstance(@RequestBody AdminInstanceUpdateRequest request) {
        if (request.title() != null) {
            instanceTitle = request.title();
        }
        if (request.description() != null) {
            instanceDescription = request.description();
        }
        if (request.shortDescription() != null) {
            shortDescription = request.shortDescription();
        }
        if (request.contactEmail() != null) {
            contactEmail = request.contactEmail();
        }
        if (request.thumbnail() != null) {
            thumbnailUrl = request.thumbnail();
        }
        if (request.terms() != null) {
            terms = request.terms();
        }
        if (request.registrationsOpen() != null) {
            registrationsOpen = request.registrationsOpen();
        }
        if (request.approvalsRequired() != null) {
            approvalsRequired = request.approvalsRequired();
        }

        return ResponseEntity.ok(new AdminInstanceDto(
                instanceTitle,
                instanceDescription,
                shortDescription,
                contactEmail,
                thumbnailUrl,
                sourceUrl,
                terms,
                registrationsOpen,
                approvalsRequired
        ));
    }

    // DTOs
    public record AdminInstanceDto(
            String title,
            String description,
            String shortDescription,
            String contactEmail,
            String thumbnail,
            String sourceUrl,
            String terms,
            boolean registrationsOpen,
            boolean approvalsRequired) {}

    public record AdminInstanceUpdateRequest(
            String title,
            String description,
            String shortDescription,
            String contactEmail,
            String thumbnail,
            String terms,
            Boolean registrationsOpen,
            Boolean approvalsRequired) {}
}
