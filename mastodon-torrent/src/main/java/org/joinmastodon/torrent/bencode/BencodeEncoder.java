package org.joinmastodon.torrent.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Encodes Java objects to bencode format.
 * 
 * Bencode supports four data types:
 * - Integers: i<integer>e (e.g., i42e)
 * - Strings: <length>:<string> (e.g., 4:spam)
 * - Lists: l<elements>e (e.g., l4:spam3:egge)
 * - Dictionaries: d<key-value pairs>e (e.g., d3:bar4:spam3:fooi42ee)
 */
public class BencodeEncoder {

    /**
     * Encode a value to bencode format.
     *
     * @param value the value to encode (Integer, Long, String, byte[], List, or Map)
     * @return the bencoded bytes
     * @throws BencodeException if encoding fails
     */
    public byte[] encode(Object value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encodeValue(value, out);
        return out.toByteArray();
    }

    /**
     * Encode a value to bencode format and write to the output stream.
     *
     * @param value the value to encode
     * @param out   the output stream to write to
     * @throws BencodeException if encoding fails
     */
    public void encode(Object value, OutputStream out) {
        encodeValue(value, out);
    }

    /**
     * Encode a value and write to the output stream.
     */
    private void encodeValue(Object value, OutputStream out) {
        try {
            if (value == null) {
                throw new BencodeException("Cannot encode null value");
            }
            
            if (value instanceof Integer) {
                encodeInteger((Integer) value, out);
            } else if (value instanceof Long) {
                encodeLong((Long) value, out);
            } else if (value instanceof String) {
                encodeString((String) value, out);
            } else if (value instanceof byte[]) {
                encodeBytes((byte[]) value, out);
            } else if (value instanceof List) {
                encodeList((List<?>) value, out);
            } else if (value instanceof Map) {
                encodeDictionary((Map<?, ?>) value, out);
            } else {
                throw new BencodeException("Unsupported type: " + value.getClass().getName());
            }
        } catch (IOException e) {
            throw new BencodeException("Failed to encode value: " + value, e);
        }
    }

    /**
     * Encode an integer.
     * Format: i<integer>e
     */
    private void encodeInteger(int value, OutputStream out) throws IOException {
        out.write('i');
        out.write(Integer.toString(value).getBytes(StandardCharsets.US_ASCII));
        out.write('e');
    }

    /**
     * Encode a long.
     * Format: i<long>e
     */
    private void encodeLong(long value, OutputStream out) throws IOException {
        out.write('i');
        out.write(Long.toString(value).getBytes(StandardCharsets.US_ASCII));
        out.write('e');
    }

    /**
     * Encode a string.
     * Format: <length>:<string>
     */
    private void encodeString(String value, OutputStream out) throws IOException {
        encodeBytes(value.getBytes(StandardCharsets.UTF_8), out);
    }

    /**
     * Encode bytes.
     * Format: <length>:<bytes>
     */
    private void encodeBytes(byte[] value, OutputStream out) throws IOException {
        out.write(Integer.toString(value.length).getBytes(StandardCharsets.US_ASCII));
        out.write(':');
        out.write(value);
    }

    /**
     * Encode a list.
     * Format: l<elements>e
     */
    private void encodeList(List<?> value, OutputStream out) throws IOException {
        out.write('l');
        for (Object element : value) {
            encodeValue(element, out);
        }
        out.write('e');
    }

    /**
     * Encode a dictionary.
     * Format: d<key-value pairs>e
     * 
     * Keys must be strings and are sorted in lexicographic order per BEP 3.
     */
    @SuppressWarnings("unchecked")
    private void encodeDictionary(Map<?, ?> value, OutputStream out) throws IOException {
        out.write('d');
        
        // Sort keys lexicographically (required by BEP 3)
        List<Map.Entry<?, ?>> sortedEntries = new ArrayList<>(value.entrySet());
        sortedEntries.sort((e1, e2) -> {
            String k1 = e1.getKey() instanceof byte[] 
                ? new String((byte[]) e1.getKey(), StandardCharsets.UTF_8)
                : e1.getKey().toString();
            String k2 = e2.getKey() instanceof byte[]
                ? new String((byte[]) e2.getKey(), StandardCharsets.UTF_8)
                : e2.getKey().toString();
            return k1.compareTo(k2);
        });
        
        for (Map.Entry<?, ?> entry : sortedEntries) {
            Object key = entry.getKey();
            // Keys must be strings or bytes
            if (key instanceof String) {
                encodeString((String) key, out);
            } else if (key instanceof byte[]) {
                encodeBytes((byte[]) key, out);
            } else {
                throw new BencodeException("Dictionary keys must be strings or bytes, got: " 
                    + key.getClass().getName());
            }
            encodeValue(entry.getValue(), out);
        }
        
        out.write('e');
    }
}
