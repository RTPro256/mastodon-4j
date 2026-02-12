package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicKey {
    private String id;
    private String owner;
    private String publicKeyPem;

    public PublicKey() {
    }

    public PublicKey(String id, String owner, String publicKeyPem) {
        this.id = id;
        this.owner = owner;
        this.publicKeyPem = publicKeyPem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }
}
