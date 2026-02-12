package org.joinmastodon.federation.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.joinmastodon.activitypub.signature.PemUtils;
import org.joinmastodon.federation.config.FederationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FederationKeyService {
    private static final Logger log = LoggerFactory.getLogger(FederationKeyService.class);

    private final FederationKeys federationKeys;
    private final String publicKeyPem;

    public FederationKeyService(FederationProperties properties) {
        KeyPair keyPair = loadOrGenerate(properties);
        String keyId = properties.getKeyId();
        if (keyId == null || keyId.isBlank()) {
            keyId = properties.getBaseUrl().replaceAll("/$", "") + "/actor#main-key";
        }
        this.federationKeys = new FederationKeys(keyPair, keyId);
        this.publicKeyPem = PemUtils.toPublicKeyPem(keyPair.getPublic());
    }

    public FederationKeys getFederationKeys() {
        return federationKeys;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    private KeyPair loadOrGenerate(FederationProperties properties) {
        String publicKeyPem = properties.getPublicKeyPem();
        String privateKeyPem = properties.getPrivateKeyPem();
        if (publicKeyPem != null && !publicKeyPem.isBlank()
                && privateKeyPem != null && !privateKeyPem.isBlank()) {
            PublicKey publicKey = PemUtils.parsePublicKey(publicKeyPem);
            PrivateKey privateKey = PemUtils.parsePrivateKey(privateKeyPem);
            return new KeyPair(publicKey, privateKey);
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            log.warn("Generated ephemeral federation key pair. Configure mastodon.federation.public-key-pem/private-key-pem for stable federation.");
            return keyPair;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate federation key pair", ex);
        }
    }
}
