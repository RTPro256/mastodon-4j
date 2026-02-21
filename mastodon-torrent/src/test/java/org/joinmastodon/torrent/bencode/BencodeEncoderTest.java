package org.joinmastodon.torrent.bencode;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BencodeEncoder.
 */
class BencodeEncoderTest {

    private final BencodeEncoder encoder = new BencodeEncoder();

    @Test
    void encodeInteger() {
        byte[] result = encoder.encode(42);
        assertEquals("i42e", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeNegativeInteger() {
        byte[] result = encoder.encode(-42);
        assertEquals("i-42e", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeLong() {
        byte[] result = encoder.encode(123456789012345L);
        assertEquals("i123456789012345e", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeZero() {
        byte[] result = encoder.encode(0);
        assertEquals("i0e", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeString() {
        byte[] result = encoder.encode("spam");
        assertEquals("4:spam", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeEmptyString() {
        byte[] result = encoder.encode("");
        assertEquals("0:", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeBytes() {
        byte[] result = encoder.encode(new byte[]{1, 2, 3, 4});
        assertEquals("4:\u0001\u0002\u0003\u0004", new String(result, StandardCharsets.ISO_8859_1));
    }

    @Test
    void encodeList() {
        byte[] result = encoder.encode(List.of("spam", "eggs"));
        assertEquals("l4:spam4:eggse", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeEmptyList() {
        byte[] result = encoder.encode(List.of());
        assertEquals("le", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeNestedList() {
        byte[] result = encoder.encode(List.of(List.of("a", "b"), "c"));
        assertEquals("ll1:a1:be1:ce", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeDictionary() {
        byte[] result = encoder.encode(Map.of("bar", "spam", "foo", 42));
        // Keys must be sorted
        assertEquals("d3:bar4:spam3:fooi42ee", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeEmptyDictionary() {
        byte[] result = encoder.encode(Map.of());
        assertEquals("de", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeNestedDictionary() {
        Map<String, Object> nested = Map.of("inner", "value");
        byte[] result = encoder.encode(Map.of("outer", nested));
        assertEquals("d5:outerd5:inner5:valueee", new String(result, StandardCharsets.US_ASCII));
    }

    @Test
    void encodeComplexStructure() {
        Map<String, Object> info = Map.of(
            "name", "test.txt",
            "length", 100L,
            "piece length", 16384
        );
        byte[] result = encoder.encode(Map.of("info", info));
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void encodeNullThrows() {
        assertThrows(BencodeException.class, () -> encoder.encode(null));
    }

    @Test
    void encodeUnsupportedTypeThrows() {
        assertThrows(BencodeException.class, () -> encoder.encode(3.14));
    }
}
