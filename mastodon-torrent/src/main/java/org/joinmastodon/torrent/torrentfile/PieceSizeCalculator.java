package org.joinmastodon.torrent.torrentfile;

/**
 * Calculates the optimal piece size for torrent files.
 * 
 * Piece size affects torrent efficiency:
 * - Smaller pieces: More hashes, larger .torrent file, but better verification granularity
 * - Larger pieces: Fewer hashes, smaller .torrent file, but more data to re-download on corruption
 * 
 * The goal is to keep the .torrent file under ~50KB while maintaining reasonable piece sizes.
 */
public class PieceSizeCalculator {

    // Minimum piece size: 16 KB
    private static final int MIN_PIECE_SIZE = 16 * 1024;
    
    // Maximum piece size: 16 MB
    private static final int MAX_PIECE_SIZE = 16 * 1024 * 1024;
    
    // Target pieces count range
    private static final int MIN_PIECES = 1000;
    private static final int MAX_PIECES = 2200;
    
    // Target .torrent file size (approximate)
    private static final long TARGET_TORRENT_SIZE = 50 * 1024;

    /**
     * Calculate the optimal piece size for a given total file size.
     * 
     * Uses a tiered approach based on file size:
     * - < 1 MB: 32 KB
     * - 1 MB - 10 MB: 64 KB
     * - 10 MB - 100 MB: 256 KB
     * - 100 MB - 1 GB: 512 KB
     * - 1 GB - 10 GB: 1 MB
     * - > 10 GB: 2 MB
     *
     * @param totalSize the total size of files in bytes
     * @return the optimal piece size in bytes
     */
    public int calculatePieceSize(long totalSize) {
        if (totalSize < 0) {
            throw new IllegalArgumentException("Total size cannot be negative");
        }
        
        if (totalSize == 0) {
            return MIN_PIECE_SIZE;
        }
        
        // Tiered approach based on total size
        if (totalSize < 1_000_000) {
            // < 1 MB: 32 KB
            return 32 * 1024;
        } else if (totalSize < 10_000_000) {
            // 1 MB - 10 MB: 64 KB
            return 64 * 1024;
        } else if (totalSize < 100_000_000) {
            // 10 MB - 100 MB: 256 KB
            return 256 * 1024;
        } else if (totalSize < 1_000_000_000) {
            // 100 MB - 1 GB: 512 KB
            return 512 * 1024;
        } else if (totalSize < 10_000_000_000L) {
            // 1 GB - 10 GB: 1 MB
            return 1024 * 1024;
        } else {
            // > 10 GB: 2 MB
            return 2 * 1024 * 1024;
        }
    }

    /**
     * Calculate piece size dynamically to achieve target piece count.
     * 
     * This method calculates the piece size that will result in approximately
     * the target number of pieces (between MIN_PIECES and MAX_PIECES).
     *
     * @param totalSize the total size of files in bytes
     * @return the calculated piece size in bytes
     */
    public int calculateDynamicPieceSize(long totalSize) {
        if (totalSize <= 0) {
            return MIN_PIECE_SIZE;
        }
        
        // Start with the tiered approach
        int pieceSize = calculatePieceSize(totalSize);
        
        // Adjust to keep piece count in target range
        long pieceCount = calculatePieceCount(totalSize, pieceSize);
        
        while (pieceCount > MAX_PIECES && pieceSize < MAX_PIECE_SIZE) {
            pieceSize *= 2;
            pieceCount = calculatePieceCount(totalSize, pieceSize);
        }
        
        while (pieceCount < MIN_PIECES && pieceSize > MIN_PIECE_SIZE) {
            pieceSize /= 2;
            pieceCount = calculatePieceCount(totalSize, pieceSize);
        }
        
        return pieceSize;
    }

    /**
     * Calculate the number of pieces for a given total size and piece size.
     *
     * @param totalSize the total size in bytes
     * @param pieceSize the piece size in bytes
     * @return the number of pieces
     */
    public long calculatePieceCount(long totalSize, int pieceSize) {
        if (pieceSize <= 0) {
            throw new IllegalArgumentException("Piece size must be positive");
        }
        return (totalSize + pieceSize - 1) / pieceSize;
    }

    /**
     * Calculate the estimated .torrent file size.
     * 
     * The .torrent file size is approximately:
     * - 20 bytes per piece (SHA-1 hash)
     * - Plus overhead for other metadata
     *
     * @param pieceCount the number of pieces
     * @return the estimated .torrent file size in bytes
     */
    public long estimateTorrentFileSize(long pieceCount) {
        // Each piece has a 20-byte SHA-1 hash
        // Plus overhead for other metadata (approximately 500 bytes)
        return (pieceCount * 20) + 500;
    }

    /**
     * Check if a piece size is valid (power of 2 and within bounds).
     *
     * @param pieceSize the piece size to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPieceSize(int pieceSize) {
        if (pieceSize < MIN_PIECE_SIZE || pieceSize > MAX_PIECE_SIZE) {
            return false;
        }
        
        // Must be a power of 2
        return (pieceSize & (pieceSize - 1)) == 0;
    }

    /**
     * Round a piece size to the nearest valid power of 2.
     *
     * @param pieceSize the piece size to round
     * @return the nearest valid piece size
     */
    public int roundToValidPieceSize(int pieceSize) {
        if (pieceSize <= MIN_PIECE_SIZE) {
            return MIN_PIECE_SIZE;
        }
        if (pieceSize >= MAX_PIECE_SIZE) {
            return MAX_PIECE_SIZE;
        }
        
        // Find the nearest power of 2
        int lower = Integer.highestOneBit(pieceSize);
        int upper = lower << 1;
        
        if (pieceSize - lower < upper - pieceSize) {
            return Math.max(lower, MIN_PIECE_SIZE);
        } else {
            return Math.min(upper, MAX_PIECE_SIZE);
        }
    }
}
