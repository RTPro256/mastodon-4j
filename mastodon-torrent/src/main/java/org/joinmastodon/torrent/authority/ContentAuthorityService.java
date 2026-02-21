package org.joinmastodon.torrent.authority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;

/**
 * Service for content authority verification.
 * 
 * Provides cryptographic signing and verification of torrent content
 * to prove content originated from a specific user on this server.
 */
public class ContentAuthorityService {

    private static final Logger log = LoggerFactory.getLogger(ContentAuthorityService.class);
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private final KeyPair serverKeyPair;
    private final String serverName;
    private final String serverUrl;

    /**
     * Create a new ContentAuthorityService.
     *
     * @param serverKeyPair the server's RSA key pair for signing
     * @param serverName the name of this Mastodon server
     * @param serverUrl the base URL of this server
     */
    public ContentAuthorityService(KeyPair serverKeyPair, String serverName, String serverUrl) {
        this.serverKeyPair = serverKeyPair;
        this.serverName = serverName;
        this.serverUrl = serverUrl;
    }

    /**
     * Create a new ContentAuthorityService with auto-generated keys.
     * Note: In production, keys should be loaded from secure storage.
     *
     * @param serverName the name of this Mastodon server
     * @param serverUrl the base URL of this server
     * @throws NoSuchAlgorithmException if RSA is not available
     */
    public ContentAuthorityService(String serverName, String serverUrl) throws NoSuchAlgorithmException {
        this(generateKeyPair(), serverName, serverUrl);
    }

    /**
     * Generate a new RSA key pair.
     */
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Sign content and create a content authority record.
     *
     * @param request the content authority request
     * @return the content authority record with signature
     */
    public ContentAuthorityRecord signContent(ContentAuthorityRequest request) {
        try {
            // Build the content to sign
            String contentToSign = buildContentToSign(
                request.getInfohash(),
                request.getCreatorAccountId(),
                request.getCreatorUsername(),
                request.getCreatedAt()
            );

            // Sign the content
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(serverKeyPair.getPrivate());
            signature.update(contentToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();

            // Create the record
            return ContentAuthorityRecord.builder()
                .infohash(request.getInfohash())
                .creatorAccountId(request.getCreatorAccountId())
                .creatorUsername(request.getCreatorUsername())
                .createdAt(request.getCreatedAt())
                .serverName(serverName)
                .serverUrl(serverUrl)
                .signature(Base64.getEncoder().encodeToString(signatureBytes))
                .signatureAlgorithm(SIGNATURE_ALGORITHM)
                .serverPublicKey(exportPublicKey(serverKeyPair.getPublic()))
                .build();

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error("Failed to sign content: {}", e.getMessage(), e);
            throw new ContentAuthorityException("Failed to sign content", e);
        }
    }

    /**
     * Verify a content authority record.
     *
     * @param record the content authority record to verify
     * @return true if the signature is valid
     */
    public boolean verifyContent(ContentAuthorityRecord record) {
        try {
            // Build the content that was signed
            String contentToVerify = buildContentToSign(
                record.getInfohash(),
                record.getCreatorAccountId(),
                record.getCreatorUsername(),
                record.getCreatedAt()
            );

            // Parse the public key
            PublicKey publicKey = importPublicKey(record.getServerPublicKey());

            // Verify the signature
            Signature signature = Signature.getInstance(record.getSignatureAlgorithm());
            signature.initVerify(publicKey);
            signature.update(contentToVerify.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(record.getSignature());
            return signature.verify(signatureBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException 
                 | InvalidKeySpecException | IllegalArgumentException e) {
            log.error("Failed to verify content: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Build the content string to be signed.
     */
    private String buildContentToSign(String infohash, Long creatorAccountId, 
                                       String creatorUsername, long createdAt) {
        return String.format("%s:%d:%s:%d:%s",
            infohash,
            creatorAccountId,
            creatorUsername,
            createdAt,
            serverName
        );
    }

    /**
     * Export a public key to a Base64 string.
     */
    private String exportPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Import a public key from a Base64 string.
     */
    private PublicKey importPublicKey(String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Get the server's public key for external verification.
     */
    public String getServerPublicKey() {
        return exportPublicKey(serverKeyPair.getPublic());
    }

    /**
     * Get the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the server URL.
     */
    public String getServerUrl() {
        return serverUrl;
    }
}
