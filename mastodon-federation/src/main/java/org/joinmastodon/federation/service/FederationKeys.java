package org.joinmastodon.federation.service;

import java.security.KeyPair;

public class FederationKeys {
    private final KeyPair keyPair;
    private final String keyId;

    public FederationKeys(KeyPair keyPair, String keyId) {
        this.keyPair = keyPair;
        this.keyId = keyId;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public String getKeyId() {
        return keyId;
    }
}
