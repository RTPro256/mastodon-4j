package org.joinmastodon.torrent.authority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContentAuthorityService.
 */
class ContentAuthorityServiceTest {

    private ContentAuthorityService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ContentAuthorityService("TestMastodon", "https://test.social");
    }

    @Test
    void signContent() {
        ContentAuthorityRequest request = ContentAuthorityRequest.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .build();

        ContentAuthorityRecord record = service.signContent(request);

        assertNotNull(record);
        assertEquals("0123456789abcdef0123456789abcdef01234567", record.getInfohash());
        assertEquals(12345L, record.getCreatorAccountId());
        assertEquals("testuser", record.getCreatorUsername());
        assertEquals("TestMastodon", record.getServerName());
        assertEquals("https://test.social", record.getServerUrl());
        assertNotNull(record.getSignature());
        assertEquals("SHA256withRSA", record.getSignatureAlgorithm());
        assertNotNull(record.getServerPublicKey());
    }

    @Test
    void verifyContentWithValidSignature() {
        ContentAuthorityRequest request = ContentAuthorityRequest.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .build();

        ContentAuthorityRecord record = service.signContent(request);
        assertTrue(service.verifyContent(record));
    }

    @Test
    void verifyContentWithTamperedInfohash() {
        ContentAuthorityRequest request = ContentAuthorityRequest.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .build();

        ContentAuthorityRecord record = service.signContent(request);
        
        // Tamper with the infohash
        ContentAuthorityRecord tamperedRecord = ContentAuthorityRecord.builder()
            .infohash("fedcba9876543210fedcba9876543210fedcba98")
            .creatorAccountId(record.getCreatorAccountId())
            .creatorUsername(record.getCreatorUsername())
            .createdAt(record.getCreatedAt())
            .serverName(record.getServerName())
            .serverUrl(record.getServerUrl())
            .signature(record.getSignature())
            .signatureAlgorithm(record.getSignatureAlgorithm())
            .serverPublicKey(record.getServerPublicKey())
            .build();

        assertFalse(service.verifyContent(tamperedRecord));
    }

    @Test
    void verifyContentWithTamperedUsername() {
        ContentAuthorityRequest request = ContentAuthorityRequest.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .build();

        ContentAuthorityRecord record = service.signContent(request);
        
        // Tamper with the username
        ContentAuthorityRecord tamperedRecord = ContentAuthorityRecord.builder()
            .infohash(record.getInfohash())
            .creatorAccountId(record.getCreatorAccountId())
            .creatorUsername("hacker")
            .createdAt(record.getCreatedAt())
            .serverName(record.getServerName())
            .serverUrl(record.getServerUrl())
            .signature(record.getSignature())
            .signatureAlgorithm(record.getSignatureAlgorithm())
            .serverPublicKey(record.getServerPublicKey())
            .build();

        assertFalse(service.verifyContent(tamperedRecord));
    }

    @Test
    void verifyContentWithInvalidSignature() {
        ContentAuthorityRequest request = ContentAuthorityRequest.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .build();

        ContentAuthorityRecord record = service.signContent(request);
        
        // Create a record with invalid signature
        ContentAuthorityRecord invalidRecord = ContentAuthorityRecord.builder()
            .infohash(record.getInfohash())
            .creatorAccountId(record.getCreatorAccountId())
            .creatorUsername(record.getCreatorUsername())
            .createdAt(record.getCreatedAt())
            .serverName(record.getServerName())
            .serverUrl(record.getServerUrl())
            .signature("invalidsignature")
            .signatureAlgorithm(record.getSignatureAlgorithm())
            .serverPublicKey(record.getServerPublicKey())
            .build();

        assertFalse(service.verifyContent(invalidRecord));
    }

    @Test
    void getServerPublicKey() {
        String publicKey = service.getServerPublicKey();
        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
    }

    @Test
    void getServerName() {
        assertEquals("TestMastodon", service.getServerName());
    }

    @Test
    void getServerUrl() {
        assertEquals("https://test.social", service.getServerUrl());
    }

    @Test
    void requestBuilderRequiresInfohash() {
        assertThrows(IllegalArgumentException.class, () ->
            ContentAuthorityRequest.builder()
                .creatorAccountId(12345L)
                .creatorUsername("testuser")
                .build()
        );
    }

    @Test
    void requestBuilderRequiresAccountId() {
        assertThrows(IllegalArgumentException.class, () ->
            ContentAuthorityRequest.builder()
                .infohash("0123456789abcdef0123456789abcdef01234567")
                .creatorUsername("testuser")
                .build()
        );
    }

    @Test
    void requestBuilderRequiresUsername() {
        assertThrows(IllegalArgumentException.class, () ->
            ContentAuthorityRequest.builder()
                .infohash("0123456789abcdef0123456789abcdef01234567")
                .creatorAccountId(12345L)
                .build()
        );
    }

    @Test
    void recordVerificationUrl() {
        ContentAuthorityRecord record = ContentAuthorityRecord.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .serverUrl("https://test.social")
            .build();

        assertEquals(
            "https://test.social/api/v1/torrents/authority/0123456789abcdef0123456789abcdef01234567",
            record.getVerificationUrl()
        );
    }
}
