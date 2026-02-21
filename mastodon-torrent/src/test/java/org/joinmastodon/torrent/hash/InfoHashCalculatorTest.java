package org.joinmastodon.torrent.hash;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InfoHashCalculator.
 */
class InfoHashCalculatorTest {

    private final InfoHashCalculator calculator = new InfoHashCalculator();

    @Test
    void calculateInfoHash() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "test.txt");
        info.put("length", 100L);
        info.put("piece length", 16384);
        info.put("pieces", new byte[20]); // 20-byte hash

        String infoHash = calculator.calculateInfoHash(info);
        
        assertNotNull(infoHash);
        assertEquals(40, infoHash.length());
        assertTrue(infoHash.matches("[0-9a-f]+"));
    }

    @Test
    void calculateInfoHashBytes() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "test");
        info.put("length", 0L);
        info.put("piece length", 16384);
        info.put("pieces", new byte[0]);

        byte[] hash = calculator.calculateInfoHashBytes(info);
        
        assertNotNull(hash);
        assertEquals(20, hash.length); // SHA-1 produces 20 bytes
    }

    @Test
    void consistentHash() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "test.txt");
        info.put("length", 1000L);
        info.put("piece length", 16384);
        info.put("pieces", new byte[20]);

        String hash1 = calculator.calculateInfoHash(info);
        String hash2 = calculator.calculateInfoHash(info);
        
        assertEquals(hash1, hash2);
    }

    @Test
    void differentInfoDifferentHash() {
        Map<String, Object> info1 = new LinkedHashMap<>();
        info1.put("name", "test1.txt");
        info1.put("length", 100L);
        info1.put("piece length", 16384);
        info1.put("pieces", new byte[20]);

        Map<String, Object> info2 = new LinkedHashMap<>();
        info2.put("name", "test2.txt");
        info2.put("length", 100L);
        info2.put("piece length", 16384);
        info2.put("pieces", new byte[20]);

        String hash1 = calculator.calculateInfoHash(info1);
        String hash2 = calculator.calculateInfoHash(info2);
        
        assertNotEquals(hash1, hash2);
    }

    @Test
    void isValidInfoHash() {
        assertTrue(InfoHashCalculator.isValidInfoHash("0123456789abcdef0123456789abcdef01234567"));
        assertTrue(InfoHashCalculator.isValidInfoHash("ABCDEF0123456789ABCDEF0123456789ABCDEF01"));
        assertFalse(InfoHashCalculator.isValidInfoHash("short"));
        assertFalse(InfoHashCalculator.isValidInfoHash("invalid-hash-with-wrong-characters-!@#"));
        assertFalse(InfoHashCalculator.isValidInfoHash(null));
        assertFalse(InfoHashCalculator.isValidInfoHash("0123456789abcdef0123456789abcdef012345678")); // 39 chars
    }

    @Test
    void hexToBytesRoundTrip() {
        String hex = "0123456789abcdef0123456789abcdef01234567";
        byte[] bytes = InfoHashCalculator.hexToBytes(hex);
        String backToHex = bytesToHex(bytes);
        assertEquals(hex, backToHex);
    }

    @Test
    void hexToBytesInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> 
            InfoHashCalculator.hexToBytes("abc"));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
