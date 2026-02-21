package org.joinmastodon.torrent.bencode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes bencode format to Java objects.
 * 
 * Bencode supports four data types:
 * - Integers: i<integer>e (e.g., i42e) -> Long
 * - Strings: <length>:<string> (e.g., 4:spam) -> byte[]
 * - Lists: l<elements>e (e.g., l4:spam3:egge) -> List<Object>
 * - Dictionaries: d<key-value pairs>e (e.g., d3:bar4:spam3:fooi42ee) -> Map<byte[], Object>
 */
public class BencodeDecoder {

    /**
     * Decode bencoded bytes to a Java object.
     *
     * @param data the bencoded data
     * @return the decoded object (Long, byte[], List, or Map)
     * @throws BencodeException if decoding fails
     */
    public Object decode(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Object result = decodeValue(in);
        
        // Ensure all data was consumed
        if (in.available() > 0) {
            throw new BencodeException("Unexpected trailing data after bencode value");
        }
        
        return result;
    }

    /**
     * Decode a bencoded value from an input stream.
     *
     * @param in the input stream
     * @return the decoded object
     * @throws BencodeException if decoding fails
     */
    public Object decode(InputStream in) {
        return decodeValue(in);
    }

    /**
     * Decode a bencoded value, detecting the type from the prefix.
     */
    private Object decodeValue(InputStream in) {
        try {
            int prefix = in.read();
            if (prefix == -1) {
                throw new BencodeException("Unexpected end of stream");
            }
            
            return switch (prefix) {
                case 'i' -> decodeInteger(in);
                case 'l' -> decodeList(in);
                case 'd' -> decodeDictionary(in);
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> decodeString(in, prefix);
                default -> throw new BencodeException("Invalid bencode prefix: " + (char) prefix);
            };
        } catch (IOException e) {
            throw new BencodeException("Failed to decode value", e);
        }
    }

    /**
     * Decode an integer.
     * Format: i<integer>e
     */
    private Long decodeInteger(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        
        while ((c = in.read()) != 'e') {
            if (c == -1) {
                throw new BencodeException("Unexpected end of stream while decoding integer");
            }
            sb.append((char) c);
        }
        
        try {
            return Long.parseLong(sb.toString());
        } catch (NumberFormatException e) {
            throw new BencodeException("Invalid integer format: " + sb, e);
        }
    }

    /**
     * Decode a string (as bytes).
     * Format: <length>:<string>
     */
    private byte[] decodeString(InputStream in, int firstDigit) throws IOException {
        // Read the length
        StringBuilder lengthStr = new StringBuilder();
        lengthStr.append((char) firstDigit);
        
        int c;
        while ((c = in.read()) != ':') {
            if (c == -1) {
                throw new BencodeException("Unexpected end of stream while decoding string length");
            }
            lengthStr.append((char) c);
        }
        
        int length;
        try {
            length = Integer.parseInt(lengthStr.toString());
        } catch (NumberFormatException e) {
            throw new BencodeException("Invalid string length: " + lengthStr, e);
        }
        
        // Read the string bytes
        byte[] bytes = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            int read = in.read(bytes, totalRead, length - totalRead);
            if (read == -1) {
                throw new BencodeException("Unexpected end of stream while reading string data");
            }
            totalRead += read;
        }
        
        return bytes;
    }

    /**
     * Decode a list.
     * Format: l<elements>e
     */
    private List<Object> decodeList(InputStream in) throws IOException {
        List<Object> list = new ArrayList<>();
        
        while (true) {
            // Peek at the next character
            in.mark(1);
            int c = in.read();
            if (c == 'e') {
                return list;
            }
            in.reset();
            
            list.add(decodeValue(in));
        }
    }

    /**
     * Decode a dictionary.
     * Format: d<key-value pairs>e
     * 
     * Keys are byte arrays, values can be any bencode type.
     */
    private Map<byte[], Object> decodeDictionary(InputStream in) throws IOException {
        Map<byte[], Object> map = new LinkedHashMap<>();
        
        while (true) {
            // Peek at the next character
            in.mark(1);
            int c = in.read();
            if (c == 'e') {
                return map;
            }
            in.reset();
            
            // Decode key (must be a string)
            Object key = decodeValue(in);
            if (!(key instanceof byte[])) {
                throw new BencodeException("Dictionary key must be a string, got: " + key.getClass().getName());
            }
            
            // Decode value
            Object value = decodeValue(in);
            
            map.put((byte[]) key, value);
        }
    }

    /**
     * Decode a bencoded value and convert byte arrays to strings where appropriate.
     * This is useful for working with torrent files where keys are typically strings.
     *
     * @param data the bencoded data
     * @return the decoded object with string keys
     */
    public Object decodeWithStringKeys(byte[] data) {
        return convertToStringKeys(decode(data));
    }

    /**
     * Recursively convert byte arrays to strings for dictionary keys and values.
     * Byte arrays that are not valid UTF-8 (like binary hashes) are kept as byte arrays.
     */
    private Object convertToStringKeys(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<byte[], Object> map = (Map<byte[], Object>) obj;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<byte[], Object> entry : map.entrySet()) {
                String key = new String(entry.getKey(), StandardCharsets.UTF_8);
                result.put(key, convertToStringKeys(entry.getValue()));
            }
            return result;
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(convertToStringKeys(item));
            }
            return result;
        } else if (obj instanceof byte[]) {
            // Try to convert byte arrays to strings if they're valid UTF-8 text
            byte[] bytes = (byte[]) obj;
            if (isValidUtf8Text(bytes)) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return bytes;
        } else {
            return obj;
        }
    }
    
    /**
     * Check if a byte array contains valid UTF-8 text (not binary data).
     * This is a heuristic that checks for common text patterns.
     */
    private boolean isValidUtf8Text(byte[] bytes) {
        if (bytes.length == 0) {
            return true; // Empty is valid text
        }
        
        // Check if all bytes are printable ASCII or valid UTF-8
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // Printable ASCII range (space to tilde, plus newline and tab)
            if (b >= 32 && b <= 126) {
                continue;
            }
            // Allow common whitespace
            if (b == '\n' || b == '\r' || b == '\t') {
                continue;
            }
            // UTF-8 continuation bytes (10xxxxxx)
            if ((b & 0xC0) == 0x80) {
                continue;
            }
            // UTF-8 start bytes for multi-byte sequences
            if ((b & 0xE0) == 0xC0 || (b & 0xF0) == 0xE0 || (b & 0xF8) == 0xF0) {
                continue;
            }
            // Non-printable byte that's not part of UTF-8 - likely binary
            return false;
        }
        return true;
    }
}
