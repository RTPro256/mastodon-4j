package org.joinmastodon.torrent.readme;

import org.joinmastodon.torrent.authority.ContentAuthorityRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TorrentReadmeGenerator.
 */
class TorrentReadmeGeneratorTest {

    private TorrentReadmeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TorrentReadmeGenerator(
            "TestMastodon",
            "https://test.social",
            "/api/v1/torrents/authority"
        );
    }

    @Test
    void generateReadmeWithBasicInfo() {
        ReadmeRequest request = ReadmeRequest.builder()
            .contentTitle("My Video")
            .creatorUsername("testuser")
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .build();

        byte[] readme = generator.generateReadme(request);
        String content = new String(readme, StandardCharsets.UTF_8);

        assertTrue(content.contains("# My Video"));
        assertTrue(content.contains("TestMastodon"));
        assertTrue(content.contains("https://test.social"));
        assertTrue(content.contains("@testuser"));
        assertTrue(content.contains("0123456789abcdef0123456789abcdef01234567"));
        assertTrue(content.contains("Content Authority Verification"));
    }

    @Test
    void generateReadmeWithAllFields() {
        ReadmeRequest request = ReadmeRequest.builder()
            .contentTitle("My Video")
            .contentDescription("A test video for demonstration")
            .contentType("Video")
            .creatorUsername("testuser")
            .creatorDisplayName("Test User")
            .creatorAccountId(12345L)
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .totalSize(1024L * 1024L * 100L) // 100 MB
            .license("CC-BY-4.0")
            .build();

        byte[] readme = generator.generateReadme(request);
        String content = new String(readme, StandardCharsets.UTF_8);

        assertTrue(content.contains("Test User"));
        assertTrue(content.contains("12345"));
        assertTrue(content.contains("100.00 MB"));
        assertTrue(content.contains("CC-BY-4.0"));
        assertTrue(content.contains("A test video for demonstration"));
    }

    @Test
    void generateReadmeWithContentAuthority() {
        ReadmeRequest request = ReadmeRequest.builder()
            .contentTitle("My Video")
            .creatorUsername("testuser")
            .creatorAccountId(12345L)
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .build();

        ContentAuthorityRecord record = ContentAuthorityRecord.builder()
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .creatorAccountId(12345L)
            .creatorUsername("testuser")
            .createdAt(System.currentTimeMillis())
            .serverName("TestMastodon")
            .serverUrl("https://test.social")
            .signature("abc123signature")
            .signatureAlgorithm("SHA256withRSA")
            .serverPublicKey("publickeybase64")
            .build();

        byte[] readme = generator.generateReadme(request, record);
        String content = new String(readme, StandardCharsets.UTF_8);

        assertTrue(content.contains("cryptographically signed"));
        assertTrue(content.contains("SHA256withRSA"));
        assertTrue(content.contains("abc123signature"));
        assertTrue(content.contains("publickeybase64"));
        assertTrue(content.contains("Programmatic Verification"));
    }

    @Test
    void readmeContainsVerificationUrl() {
        ReadmeRequest request = ReadmeRequest.builder()
            .contentTitle("My Video")
            .creatorUsername("testuser")
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .build();

        byte[] readme = generator.generateReadme(request);
        String content = new String(readme, StandardCharsets.UTF_8);

        assertTrue(content.contains("https://test.social/api/v1/torrents/authority/0123456789abcdef0123456789abcdef01234567"));
    }

    @Test
    void readmeContainsAccountUrl() {
        ReadmeRequest request = ReadmeRequest.builder()
            .contentTitle("My Video")
            .creatorUsername("testuser")
            .infohash("0123456789abcdef0123456789abcdef01234567")
            .build();

        byte[] readme = generator.generateReadme(request);
        String content = new String(readme, StandardCharsets.UTF_8);

        assertTrue(content.contains("https://test.social/@testuser"));
    }

    @Test
    void getReadmeFilename() {
        assertEquals("README.md", generator.getReadmeFilename());
    }

    @Test
    void requestBuilderRequiresTitle() {
        assertThrows(IllegalArgumentException.class, () -> 
            ReadmeRequest.builder()
                .creatorUsername("testuser")
                .infohash("0123456789abcdef0123456789abcdef01234567")
                .build()
        );
    }

    @Test
    void requestBuilderRequiresUsername() {
        assertThrows(IllegalArgumentException.class, () -> 
            ReadmeRequest.builder()
                .contentTitle("My Video")
                .infohash("0123456789abcdef0123456789abcdef01234567")
                .build()
        );
    }

    @Test
    void requestBuilderRequiresInfohash() {
        assertThrows(IllegalArgumentException.class, () -> 
            ReadmeRequest.builder()
                .contentTitle("My Video")
                .creatorUsername("testuser")
                .build()
        );
    }
}
