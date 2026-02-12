package org.joinmastodon.federation.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.federation.model.WebFingerLink;
import org.joinmastodon.federation.model.WebFingerResponse;
import org.springframework.stereotype.Service;

@Service
public class WebFingerService {
    private final AccountService accountService;
    private final FederationProperties properties;

    public WebFingerService(AccountService accountService, FederationProperties properties) {
        this.accountService = accountService;
        this.properties = properties;
    }

    public Optional<WebFingerResponse> resolve(String resource) {
        if (resource == null || !resource.startsWith("acct:")) {
            return Optional.empty();
        }
        String acct = resource.substring("acct:".length());
        String[] parts = acct.split("@", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }
        String username = parts[0];
        String domain = parts[1];
        if (!domain.equalsIgnoreCase(properties.getDomain())) {
            return Optional.empty();
        }
        Optional<Account> account = accountService.findByUsernameAndDomain(username, null);
        if (account.isEmpty()) {
            return Optional.empty();
        }
        String baseUrl = properties.getBaseUrl().replaceAll("/$", "");
        String actorUrl = baseUrl + "/users/" + account.get().getUsername();
        WebFingerResponse response = new WebFingerResponse();
        response.setSubject("acct:" + account.get().getUsername() + "@" + properties.getDomain().toLowerCase(Locale.ROOT));
        response.setAliases(List.of(actorUrl));
        response.setLinks(List.of(new WebFingerLink("self", "application/activity+json", actorUrl)));
        return Optional.of(response);
    }
}
