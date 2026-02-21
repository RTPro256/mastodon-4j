package org.joinmastodon.torrent.magnet;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MagnetLink.
 */
class MagnetLinkTest {

    @Test
    void createMagnetLink() {
        MagnetLink magnet = new MagnetLink("0123456789abcdef0123456789abcdef01234567");
        
        assertEquals("0123456789abcdef0123456789abcdef01234567", magnet.getInfoHash());
    }

    @Test
    void createMagnetLinkWithDisplayName() {
        MagnetLink magnet = new MagnetLink("0123456789abcdef0123456789abcdef01234567")
                .displayName("test.txt");
        
        assertEquals("test.txt", magnet.getDisplayName());
    }

    @Test
    void createMagnetLinkWithTrackers() {
        MagnetLink magnet = new MagnetLink("0123456789abcdef0123456789abcdef01234567")
                .tracker("udp://tracker.example.com:1337/announce")
                .tracker("http://tracker.example.com:6969/announce");
        
        assertEquals(2, magnet.getTrackers().size());
        assertTrue(magnet.getTrackers().contains("udp://tracker.example.com:1337/announce"));
    }

    @Test
    void createMagnetLinkWithSize() {
        MagnetLink magnet = new MagnetLink("0123456789abcdef0123456789abcdef01234567")
                .exactLength(1048576);
        
        assertEquals(1048576, magnet.getExactLength());
    }

    @Test
    void buildUri() {
        MagnetLink magnet = new MagnetLink("0123456789abcdef0123456789abcdef01234567")
                .displayName("test.txt")
                .exactLength(1000)
                .tracker("udp://tracker.example.com:1337/announce");
        
        String uri = magnet.toUri();
        
        assertTrue(uri.startsWith("magnet:?xt=urn:btih:0123456789abcdef0123456789abcdef01234567"));
        assertTrue(uri.contains("dn=test.txt"));
        assertTrue(uri.contains("xl=1000"));
        assertTrue(uri.contains("tr=udp%3A%2F%2Ftracker.example.com%3A1337%2Fannounce"));
    }

    @Test
    void parseUri() {
        String uri = "magnet:?xt=urn:btih:0123456789abcdef0123456789abcdef01234567&dn=test.txt&xl=1000";
        
        MagnetLink magnet = MagnetLink.fromUri(uri);
        
        assertEquals("0123456789abcdef0123456789abcdef01234567", magnet.getInfoHash());
        assertEquals("test.txt", magnet.getDisplayName());
        assertEquals(1000, magnet.getExactLength());
    }

    @Test
    void parseUriWithTrackers() {
        String uri = "magnet:?xt=urn:btih:0123456789abcdef0123456789abcdef01234567&tr=udp%3A%2F%2Ftracker.example.com%3A1337%2Fannounce";
        
        MagnetLink magnet = MagnetLink.fromUri(uri);
        
        assertEquals(1, magnet.getTrackers().size());
        assertEquals("udp://tracker.example.com:1337/announce", magnet.getTrackers().get(0));
    }

    @Test
    void parseInvalidUriThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            MagnetLink.fromUri("http://example.com"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            MagnetLink.fromUri("magnet:?dn=test.txt")); // Missing xt
    }

    @Test
    void invalidInfoHashThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MagnetLink("short"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new MagnetLink("invalid!chars!in!hash!not!valid!ok"));
    }

    @Test
    void isValidInfoHash() {
        assertTrue(MagnetLink.isValidInfoHash("0123456789abcdef0123456789abcdef01234567"));
        assertTrue(MagnetLink.isValidInfoHash("ABCDEF0123456789ABCDEF0123456789ABCDEF01"));
        assertFalse(MagnetLink.isValidInfoHash("short"));
        assertFalse(MagnetLink.isValidInfoHash(null));
    }

    @Test
    void roundTrip() {
        MagnetLink original = new MagnetLink("0123456789abcdef0123456789abcdef01234567")
                .displayName("test file.txt")
                .exactLength(1048576)
                .tracker("udp://tracker.example.com:1337/announce")
                .keyword("test");
        
        String uri = original.toUri();
        MagnetLink parsed = MagnetLink.fromUri(uri);
        
        assertEquals(original.getInfoHash(), parsed.getInfoHash());
        assertEquals(original.getDisplayName(), parsed.getDisplayName());
        assertEquals(original.getExactLength(), parsed.getExactLength());
        assertEquals(original.getTrackers(), parsed.getTrackers());
    }

    @Test
    void equality() {
        MagnetLink magnet1 = new MagnetLink("0123456789abcdef0123456789abcdef01234567");
        MagnetLink magnet2 = new MagnetLink("0123456789abcdef0123456789abcdef01234567");
        MagnetLink magnet3 = new MagnetLink("abcdef0123456789abcdef0123456789abcdef01");
        
        assertEquals(magnet1, magnet2);
        assertNotEquals(magnet1, magnet3);
    }
}
