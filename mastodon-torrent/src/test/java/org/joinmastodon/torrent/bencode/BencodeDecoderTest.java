package org.joinmastodon.torrent.bencode;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BencodeDecoder.
 */
class BencodeDecoderTest {

    private final BencodeDecoder decoder = new BencodeDecoder();

    @Test
    void decodeInteger() {
        Long result = (Long) decoder.decode("i42e".getBytes(StandardCharsets.US_ASCII));
        assertEquals(42L, result);
    }

    @Test
    void decodeNegativeInteger() {
        Long result = (Long) decoder.decode("i-42e".getBytes(StandardCharsets.US_ASCII));
        assertEquals(-42L, result);
    }

    @Test
    void decodeZero() {
        Long result = (Long) decoder.decode("i0e".getBytes(StandardCharsets.US_ASCII));
        assertEquals(0L, result);
    }

    @Test
    void decodeString() {
        byte[] result = (byte[]) decoder.decode("4:spam".getBytes(StandardCharsets.US_ASCII));
        assertEquals("spam", new String(result, StandardCharsets.UTF_8));
    }

    @Test
    void decodeEmptyString() {
        byte[] result = (byte[]) decoder.decode("0:".getBytes(StandardCharsets.US_ASCII));
        assertEquals(0, result.length);
    }

    @Test
    void decodeList() {
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) decoder.decode("l4:spam4:eggse".getBytes(StandardCharsets.US_ASCII));
        assertEquals(2, result.size());
        assertEquals("spam", new String((byte[]) result.get(0), StandardCharsets.UTF_8));
        assertEquals("eggs", new String((byte[]) result.get(1), StandardCharsets.UTF_8));
    }

    @Test
    void decodeEmptyList() {
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) decoder.decode("le".getBytes(StandardCharsets.US_ASCII));
        assertEquals(0, result.size());
    }

    @Test
    void decodeDictionary() {
        @SuppressWarnings("unchecked")
        Map<byte[], Object> result = (Map<byte[], Object>) decoder.decode(
            "d3:bar4:spam3:fooi42ee".getBytes(StandardCharsets.US_ASCII));
        
        assertEquals(2, result.size());
        
        // Note: byte[] keys use reference equality, so we need to iterate to find them
        boolean foundBar = false, foundFoo = false;
        for (Map.Entry<byte[], Object> entry : result.entrySet()) {
            String key = new String(entry.getKey(), StandardCharsets.UTF_8);
            if (key.equals("bar")) {
                foundBar = true;
                assertEquals("spam", new String((byte[]) entry.getValue(), StandardCharsets.UTF_8));
            } else if (key.equals("foo")) {
                foundFoo = true;
                assertEquals(42L, entry.getValue());
            }
        }
        assertTrue(foundBar, "Should have found 'bar' key");
        assertTrue(foundFoo, "Should have found 'foo' key");
    }

    @Test
    void decodeEmptyDictionary() {
        @SuppressWarnings("unchecked")
        Map<byte[], Object> result = (Map<byte[], Object>) decoder.decode("de".getBytes(StandardCharsets.US_ASCII));
        assertEquals(0, result.size());
    }

    @Test
    void decodeNestedStructure() {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) decoder.decodeWithStringKeys(
            "d5:outerd5:inner5:valueee".getBytes(StandardCharsets.US_ASCII));
        
        assertEquals(1, result.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> outer = (Map<String, Object>) result.get("outer");
        assertEquals("value", outer.get("inner"));
    }

    @Test
    void decodeWithTrailingDataThrows() {
        assertThrows(BencodeException.class, () -> 
            decoder.decode("i42eextra".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void decodeInvalidPrefixThrows() {
        assertThrows(BencodeException.class, () -> 
            decoder.decode("x42e".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void decodeInvalidIntegerThrows() {
        assertThrows(BencodeException.class, () -> 
            decoder.decode("iabce".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void encodeDecodeRoundTrip() {
        BencodeEncoder encoder = new BencodeEncoder();
        
        Map<String, Object> original = Map.of(
            "name", "test.txt",
            "length", 100L,
            "pieces", new byte[]{1, 2, 3, 4}
        );
        
        byte[] encoded = encoder.encode(original);
        @SuppressWarnings("unchecked")
        Map<String, Object> decoded = (Map<String, Object>) decoder.decodeWithStringKeys(encoded);
        
        assertEquals("test.txt", decoded.get("name"));
        assertEquals(100L, decoded.get("length"));
        assertArrayEquals(new byte[]{1, 2, 3, 4}, (byte[]) decoded.get("pieces"));
    }
}
