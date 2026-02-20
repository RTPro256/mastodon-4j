package org.joinmastodon.federation.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

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

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}
