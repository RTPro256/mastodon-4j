package org.joinmastodon.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RemoteActorService {
    private static final Logger log = LoggerFactory.getLogger(RemoteActorService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;

    public RemoteActorService(HttpClient httpClient, ObjectMapper objectMapper, AccountService accountService) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.accountService = accountService;
    }

    public Optional<Account> fetchAndStore(String actorUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(actorUrl))
                    .header("Accept", "application/activity+json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Failed to fetch actor {}: status {}", actorUrl, response.statusCode());
                return Optional.empty();
            }
            Actor actor = objectMapper.readValue(response.body(), Actor.class);
            return Optional.of(upsertActor(actor));
        } catch (Exception ex) {
            log.warn("Failed to fetch actor {}", actorUrl, ex);
            return Optional.empty();
        }
    }

    public Account upsertActor(Actor actor) {
        String actorId = actor.getId();
        String username = actor.getPreferredUsername();
        String domain = null;
        if (actorId != null) {
            try {
                URI uri = URI.create(actorId);
                domain = uri.getHost();
            } catch (Exception ignored) {
            }
        }
        Account account = accountService.findByAcct(username + "@" + domain).orElseGet(Account::new);
        account.setUsername(username);
        account.setDomain(domain);
        account.setAcct(username + "@" + domain);
        account.setDisplayName(actor.getName());
        account.setNote(actor.getSummary());
        account.setUrl(actor.getId());
        account.setAvatarUrl(actor.getIcon() == null ? null : actor.getIcon().getUrl());
        account.setHeaderUrl(actor.getImage() == null ? null : actor.getImage().getUrl());
        account.setActorUri(actor.getId());
        account.setInboxUrl(actor.getInbox());
        account.setSharedInboxUrl(actor.getEndpoints() == null ? null : String.valueOf(actor.getEndpoints().get("sharedInbox")));
        account.setPublicKeyPem(actor.getPublicKey() == null ? null : actor.getPublicKey().getPublicKeyPem());
        account.setLocalAccount(false);
        account.setLastFetchedAt(Instant.now());
        return accountService.save(account);
    }
}
