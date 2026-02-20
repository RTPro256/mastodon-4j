package org.joinmastodon.activitypub.signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Interoperability tests for HTTP Signatures.
 * Tests signature generation and verification against known patterns
 * from the ActivityPub specification and Mastodon implementation.
 * 
 * @see <a href="https://www.w3.org/wiki/SocialCG/ActivityPub/Authentication-Authorization#HTTP_Signatures">HTTP Signatures</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-cavage-http-signatures">HTTP Signatures Draft</a>
 */
@DisplayName("HTTP Signature Interoperability Tests")
class HttpSignatureInteropTest {

    private HttpSignatureSigner signer;
    private HttpSignatureVerifier verifier;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        signer = new HttpSignatureSigner();
        verifier = new HttpSignatureVerifier();
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    @Nested
    @DisplayName("Signature Generation")
    class SignatureGenerationTests {

        @Test
        @DisplayName("Generates valid signature for basic request")
        void generatesValidSignatureForBasicRequest() {
            String keyId = "https://example.com/users/alice#main-key";
            String method = "POST";
            String path = "/users/bob/inbox";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT",
                    "digest", "SHA-256=abc123"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date", "digest");

            String signatureHeader = signer.sign(keyId, privateKey, method, path, headers, signedHeaders);

            assertThat(signatureHeader).contains("keyId=\"" + keyId + "\"");
            assertThat(signatureHeader).contains("algorithm=\"rsa-sha256\"");
            assertThat(signatureHeader).contains("signature=\"");
        }

        @Test
        @DisplayName("Generates consistent signing string")
        void generatesConsistentSigningString() {
            String method = "POST";
            String path = "/inbox";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date");

            String signingString = signer.buildSigningString(method, path, headers, signedHeaders);

            assertThat(signingString).isEqualTo(
                    "(request-target): post /inbox\n" +
                    "host: example.com\n" +
                    "date: Tue, 20 Feb 2024 12:00:00 GMT"
            );
        }

        @Test
        @DisplayName("Handles case-insensitive headers")
        void handlesCaseInsensitiveHeaders() {
            String method = "GET";
            String path = "/users/alice";
            Map<String, String> headers = Map.of(
                    "Host", "Example.COM",
                    "Date", "Tue, 20 Feb 2024 12:00:00 GMT"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date");

            String signingString = signer.buildSigningString(method, path, headers, signedHeaders);

            // Headers should be normalized to lowercase
            assertThat(signingString).contains("host: Example.COM");
            assertThat(signingString).contains("date: Tue, 20 Feb 2024 12:00:00 GMT");
        }
    }

    @Nested
    @DisplayName("Signature Verification")
    class SignatureVerificationTests {

        @Test
        @DisplayName("Verifies self-signed signature")
        void verifiesSelfSignedSignature() {
            String keyId = "https://example.com/users/alice#main-key";
            String method = "POST";
            String path = "/users/bob/inbox";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT",
                    "digest", "SHA-256=abc123"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date", "digest");

            String signatureHeader = signer.sign(keyId, privateKey, method, path, headers, signedHeaders);

            HttpSignature signature = HttpSignature.parse(signatureHeader);
            assertThat(signature).isNotNull();
            assertThat(signature.getKeyId()).isEqualTo(keyId);
            assertThat(signature.getAlgorithm()).isEqualTo("rsa-sha256");

            // Verify the signature with the public key
            boolean verified = verifier.verify(signature, publicKey, method, path, headers);
            assertThat(verified).isTrue();
        }

        @Test
        @DisplayName("Rejects tampered signature")
        void rejectsTamperedSignature() {
            String keyId = "https://example.com/users/alice#main-key";
            String method = "POST";
            String path = "/inbox";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date");

            String signatureHeader = signer.sign(keyId, privateKey, method, path, headers, signedHeaders);

            // Tamper with the signature
            String tamperedSignature = signatureHeader.replace("signature=\"", "signature=\"tampered");

            HttpSignature signature = HttpSignature.parse(tamperedSignature);
            boolean verified = verifier.verify(signature, publicKey, method, path, headers);
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("Rejects signature with modified content")
        void rejectsModifiedContent() {
            String keyId = "https://example.com/users/alice#main-key";
            String method = "POST";
            String path = "/inbox";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date");

            String signatureHeader = signer.sign(keyId, privateKey, method, path, headers, signedHeaders);

            // Modify the date header
            Map<String, String> modifiedHeaders = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 13:00:00 GMT"  // Different time
            );

            HttpSignature signature = HttpSignature.parse(signatureHeader);
            boolean verified = verifier.verify(signature, publicKey, method, path, modifiedHeaders);
            assertThat(verified).isFalse();
        }
    }

    @Nested
    @DisplayName("Digest Verification")
    class DigestVerificationTests {

        @Test
        @DisplayName("Computes valid SHA-256 digest")
        void computesValidDigest() {
            String content = "{\"type\":\"Note\",\"content\":\"Hello world\"}";
            String digest = DigestUtils.buildDigestHeader(content);

            assertThat(digest).startsWith("SHA-256=");
            assertThat(digest.length()).isGreaterThan(10);

            // Verify the digest is valid
            boolean valid = DigestUtils.verifyDigest(digest, content);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Rejects modified content digest")
        void rejectsModifiedContentDigest() {
            String content = "{\"type\":\"Note\",\"content\":\"Hello world\"}";
            String digest = DigestUtils.buildDigestHeader(content);

            String modifiedContent = "{\"type\":\"Note\",\"content\":\"Hello modified world\"}";
            boolean valid = DigestUtils.verifyDigest(digest, modifiedContent);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Handles empty content")
        void handlesEmptyContent() {
            String content = "";
            String digest = DigestUtils.buildDigestHeader(content);

            assertThat(digest).startsWith("SHA-256=");
            boolean valid = DigestUtils.verifyDigest(digest, content);
            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("Header Parsing")
    class HeaderParsingTests {

        @Test
        @DisplayName("Parses signature header correctly")
        void parsesSignatureHeaderCorrectly() {
            String signatureHeader = "keyId=\"https://example.com/users/alice#main-key\"," +
                    "algorithm=\"rsa-sha256\"," +
                    "headers=\"(request-target) host date digest\"," +
                    "signature=\"abc123def456\"";

            HttpSignature signature = HttpSignature.parse(signatureHeader);

            assertThat(signature.getKeyId()).isEqualTo("https://example.com/users/alice#main-key");
            assertThat(signature.getAlgorithm()).isEqualTo("rsa-sha256");
            assertThat(signature.getHeaders()).containsExactly("(request-target)", "host", "date", "digest");
            assertThat(signature.getSignature()).isEqualTo("abc123def456");
        }

        @Test
        @DisplayName("Handles quoted strings in values")
        void handlesQuotedStrings() {
            String signatureHeader = "keyId=\"https://example.com/users/alice#main-key\"," +
                    "signature=\"base64+encoded/signature==\"";

            HttpSignature signature = HttpSignature.parse(signatureHeader);

            assertThat(signature.getSignature()).isEqualTo("base64+encoded/signature==");
        }
    }

    @Nested
    @DisplayName("Mastodon Compatibility")
    class MastodonCompatibilityTests {

        @Test
        @DisplayName("Uses Mastodon-compatible signed headers")
        void usesMastodonCompatibleHeaders() {
            // Mastodon typically signs: (request-target), host, date, digest
            String method = "POST";
            String path = "/users/bob/inbox";
            Map<String, String> headers = Map.of(
                    "host", "mastodon.social",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT",
                    "digest", "SHA-256=XcYj9Gh2kL3mN4pQ5rS6tU7vW8xY9zA0bC1dE2fG3hI="
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date", "digest");

            String signatureHeader = signer.sign(
                    "https://mastodon.social/users/alice#main-key",
                    privateKey,
                    method,
                    path,
                    headers,
                    signedHeaders
            );

            assertThat(signatureHeader).contains("headers=\"(request-target) host date digest\"");
        }

        @Test
        @DisplayName("Supports GET requests without digest")
        void supportsGetRequestsWithoutDigest() {
            String method = "GET";
            String path = "/users/alice";
            Map<String, String> headers = Map.of(
                    "host", "example.com",
                    "date", "Tue, 20 Feb 2024 12:00:00 GMT",
                    "accept", "application/activity+json"
            );
            List<String> signedHeaders = List.of("(request-target)", "host", "date", "accept");

            String signatureHeader = signer.sign(
                    "https://example.com/users/bob#main-key",
                    privateKey,
                    method,
                    path,
                    headers,
                    signedHeaders
            );

            assertThat(signatureHeader).isNotEmpty();
            assertThat(signatureHeader).contains("keyId=");
        }
    }
}
