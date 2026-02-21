package org.joinmastodon.torrent.torrentfile;

import org.joinmastodon.torrent.bencode.BencodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hashes file pieces for torrent creation.
 * 
 * Each piece of a torrent file is hashed with SHA-1, producing a 20-byte hash.
 * All piece hashes are concatenated together to form the "pieces" field in the info dictionary.
 */
public class PieceHasher {

    private static final Logger log = LoggerFactory.getLogger(PieceHasher.class);
    
    private static final int SHA1_HASH_SIZE = 20;
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Hash all pieces of a single file.
     *
     * @param filePath   the path to the file
     * @param pieceSize  the size of each piece in bytes
     * @return the concatenated SHA-1 hashes of all pieces
     * @throws IOException if reading the file fails
     */
    public byte[] hashFile(Path filePath, int pieceSize) throws IOException {
        try (InputStream in = Files.newInputStream(filePath)) {
            return hashStream(in, pieceSize, Files.size(filePath));
        }
    }

    /**
     * Hash all pieces from an input stream.
     *
     * @param in         the input stream
     * @param pieceSize  the size of each piece in bytes
     * @param totalSize  the total size of the data
     * @return the concatenated SHA-1 hashes of all pieces
     * @throws IOException if reading the stream fails
     */
    public byte[] hashStream(InputStream in, int pieceSize, long totalSize) throws IOException {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new BencodeException("SHA-1 algorithm not available", e);
        }

        List<byte[]> pieceHashes = new ArrayList<>();
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] pieceBuffer = new byte[pieceSize];
        int pieceOffset = 0;
        long totalRead = 0;
        
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            int bufferOffset = 0;
            
            while (bufferOffset < bytesRead) {
                int bytesToCopy = Math.min(pieceSize - pieceOffset, bytesRead - bufferOffset);
                System.arraycopy(buffer, bufferOffset, pieceBuffer, pieceOffset, bytesToCopy);
                pieceOffset += bytesToCopy;
                bufferOffset += bytesToCopy;
                totalRead += bytesToCopy;
                
                // If piece is complete, hash it
                if (pieceOffset == pieceSize) {
                    sha1.update(pieceBuffer, 0, pieceOffset);
                    pieceHashes.add(sha1.digest());
                    sha1.reset();
                    pieceOffset = 0;
                    
                    log.debug("Hashed piece {} of {}", pieceHashes.size(), 
                            (totalSize + pieceSize - 1) / pieceSize);
                }
            }
        }
        
        // Hash the final partial piece if any
        if (pieceOffset > 0) {
            sha1.update(pieceBuffer, 0, pieceOffset);
            pieceHashes.add(sha1.digest());
        }
        
        // Concatenate all hashes
        return concatenateHashes(pieceHashes);
    }

    /**
     * Hash multiple files as a multi-file torrent.
     *
     * @param files      list of file paths
     * @param pieceSize  the size of each piece in bytes
     * @return the concatenated SHA-1 hashes of all pieces
     * @throws IOException if reading files fails
     */
    public byte[] hashFiles(List<Path> files, int pieceSize) throws IOException {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new BencodeException("SHA-1 algorithm not available", e);
        }

        List<byte[]> pieceHashes = new ArrayList<>();
        byte[] pieceBuffer = new byte[pieceSize];
        int pieceOffset = 0;
        
        for (Path file : files) {
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    int bufferOffset = 0;
                    
                    while (bufferOffset < bytesRead) {
                        int bytesToCopy = Math.min(pieceSize - pieceOffset, bytesRead - bufferOffset);
                        System.arraycopy(buffer, bufferOffset, pieceBuffer, pieceOffset, bytesToCopy);
                        pieceOffset += bytesToCopy;
                        bufferOffset += bytesToCopy;
                        
                        // If piece is complete, hash it
                        if (pieceOffset == pieceSize) {
                            sha1.update(pieceBuffer, 0, pieceOffset);
                            pieceHashes.add(sha1.digest());
                            sha1.reset();
                            pieceOffset = 0;
                        }
                    }
                }
            }
        }
        
        // Hash the final partial piece if any
        if (pieceOffset > 0) {
            sha1.update(pieceBuffer, 0, pieceOffset);
            pieceHashes.add(sha1.digest());
        }
        
        return concatenateHashes(pieceHashes);
    }

    /**
     * Calculate the number of pieces for a given total size and piece size.
     *
     * @param totalSize the total size in bytes
     * @param pieceSize the piece size in bytes
     * @return the number of pieces
     */
    public int calculatePieceCount(long totalSize, int pieceSize) {
        return (int) ((totalSize + pieceSize - 1) / pieceSize);
    }

    /**
     * Get the expected size of the concatenated hashes.
     *
     * @param pieceCount the number of pieces
     * @return the size in bytes
     */
    public int getExpectedHashesSize(int pieceCount) {
        return pieceCount * SHA1_HASH_SIZE;
    }

    /**
     * Concatenate all piece hashes into a single byte array.
     */
    private byte[] concatenateHashes(List<byte[]> hashes) {
        byte[] result = new byte[hashes.size() * SHA1_HASH_SIZE];
        int offset = 0;
        
        for (byte[] hash : hashes) {
            if (hash.length != SHA1_HASH_SIZE) {
                throw new BencodeException("Invalid hash size: " + hash.length + 
                        ", expected " + SHA1_HASH_SIZE);
            }
            System.arraycopy(hash, 0, result, offset, SHA1_HASH_SIZE);
            offset += SHA1_HASH_SIZE;
        }
        
        return result;
    }

    /**
     * Extract individual piece hashes from the concatenated hashes.
     *
     * @param concatenatedHashes the concatenated hashes
     * @return list of individual 20-byte hashes
     */
    public List<byte[]> splitHashes(byte[] concatenatedHashes) {
        if (concatenatedHashes.length % SHA1_HASH_SIZE != 0) {
            throw new IllegalArgumentException("Invalid concatenated hashes length: " + 
                    concatenatedHashes.length);
        }
        
        List<byte[]> hashes = new ArrayList<>();
        for (int i = 0; i < concatenatedHashes.length; i += SHA1_HASH_SIZE) {
            byte[] hash = new byte[SHA1_HASH_SIZE];
            System.arraycopy(concatenatedHashes, i, hash, 0, SHA1_HASH_SIZE);
            hashes.add(hash);
        }
        
        return hashes;
    }
}
