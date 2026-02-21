package org.joinmastodon.torrent.hash;

import org.joinmastodon.torrent.bencode.BencodeEncoder;
import org.joinmastodon.torrent.bencode.BencodeException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Calculates the infohash for torrent files.
 * 
 * The infohash is a SHA-1 hash of the bencoded "info" dictionary from a torrent file.
 * It uniquely identifies the torrent and is used in magnet links and tracker announces.
 */
public class InfoHashCalculator {

    private final BencodeEncoder encoder;

    public InfoHashCalculator() {
        this.encoder = new BencodeEncoder();
    }

    public InfoHashCalculator(BencodeEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Calculate the SHA-1 infohash from an info dictionary.
     *
     * @param infoDictionary the info dictionary as a Map
     * @return the infohash as a 40-character hex string
     * @throws BencodeException if encoding fails
     */
    public String calculateInfoHash(Map<String, Object> infoDictionary) {
        byte[] hash = calculateInfoHashBytes(infoDictionary);
        return bytesToHex(hash);
    }

    /**
     * Calculate the SHA-1 infohash from an info dictionary.
     *
     * @param infoDictionary the info dictionary as a Map
     * @return the infohash as a 20-byte array
     * @throws BencodeException if encoding fails
     */
    public byte[] calculateInfoHashBytes(Map<String, Object> infoDictionary) {
        // Bencode the info dictionary
        byte[] bencodedInfo = encoder.encode(infoDictionary);
        
        // Calculate SHA-1 hash
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new BencodeException("SHA-1 algorithm not available", e);
        }
        
        return sha1.digest(bencodedInfo);
    }

    /**
     * Calculate the SHA-1 infohash from already-bencoded info dictionary.
     *
     * @param bencodedInfo the bencoded info dictionary bytes
     * @return the infohash as a 40-character hex string
     */
    public String calculateInfoHashFromBencoded(byte[] bencodedInfo) {
        byte[] hash = calculateInfoHashBytesFromBencoded(bencodedInfo);
        return bytesToHex(hash);
    }

    /**
     * Calculate the SHA-1 infohash from already-bencoded info dictionary.
     *
     * @param bencodedInfo the bencoded info dictionary bytes
     * @return the infohash as a 20-byte array
     */
    public byte[] calculateInfoHashBytesFromBencoded(byte[] bencodedInfo) {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new BencodeException("SHA-1 algorithm not available", e);
        }
        
        return sha1.digest(bencodedInfo);
    }

    /**
     * Convert a byte array to a hex string.
     *
     * @param bytes the byte array
     * @return the hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * Convert a hex string to a byte array.
     *
     * @param hex the hex string
     * @return the byte array
     * @throws IllegalArgumentException if the hex string is invalid
     */
    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

    /**
     * Validate that a string is a valid infohash.
     *
     * @param infoHash the infohash to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInfoHash(String infoHash) {
        if (infoHash == null || infoHash.length() != 40) {
            return false;
        }
        
        for (char c : infoHash.toCharArray()) {
            if (!Character.isDigit(c) && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                return false;
            }
        }
        
        return true;
    }
}
