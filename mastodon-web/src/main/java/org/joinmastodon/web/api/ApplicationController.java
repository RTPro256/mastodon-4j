package org.joinmastodon.web.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.service.ApplicationService;
import org.joinmastodon.web.api.dto.ApplicationDto;
import org.joinmastodon.web.api.dto.request.ApplicationCreateRequest;
import org.joinmastodon.web.auth.ScopeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/apps")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApplicationDto create(@Valid @RequestBody ApplicationCreateRequest request) {
        Application application = new Application();
        application.setName(request.clientName());
        application.setWebsite(request.website());
        application.setRedirectUri(request.redirectUris());
        application.setScopes(ScopeUtil.normalize(request.scopes()));
        application.setClientId(generateId());
        application.setClientSecret(generateSecret());
        Application saved = applicationService.save(application);
        return ApiMapper.toApplicationDto(saved);
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
