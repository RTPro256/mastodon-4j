package org.joinmastodon.torrent.torrentfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PieceSizeCalculator.
 */
class PieceSizeCalculatorTest {

    private final PieceSizeCalculator calculator = new PieceSizeCalculator();

    @Test
    void smallFile() {
        // < 1 MB: 32 KB
        assertEquals(32 * 1024, calculator.calculatePieceSize(500_000));
    }

    @Test
    void mediumFile() {
        // 1 MB - 10 MB: 64 KB
        assertEquals(64 * 1024, calculator.calculatePieceSize(5_000_000));
    }

    @Test
    void largeFile() {
        // 10 MB - 100 MB: 256 KB
        assertEquals(256 * 1024, calculator.calculatePieceSize(50_000_000));
    }

    @Test
    void veryLargeFile() {
        // 100 MB - 1 GB: 512 KB
        assertEquals(512 * 1024, calculator.calculatePieceSize(500_000_000));
    }

    @Test
    void hugeFile() {
        // 1 GB - 10 GB: 1 MB
        assertEquals(1024 * 1024, calculator.calculatePieceSize(5_000_000_000L));
    }

    @Test
    void enormousFile() {
        // > 10 GB: 2 MB
        assertEquals(2 * 1024 * 1024, calculator.calculatePieceSize(20_000_000_000L));
    }

    @Test
    void zeroSize() {
        assertEquals(16 * 1024, calculator.calculatePieceSize(0));
    }

    @Test
    void negativeSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            calculator.calculatePieceSize(-1));
    }

    @Test
    void calculatePieceCount() {
        // 1000 bytes with 100 byte pieces = 10 pieces
        assertEquals(10, calculator.calculatePieceCount(1000, 100));
        
        // 1001 bytes with 100 byte pieces = 11 pieces
        assertEquals(11, calculator.calculatePieceCount(1001, 100));
    }

    @Test
    void isValidPieceSize() {
        assertTrue(calculator.isValidPieceSize(32 * 1024));
        assertTrue(calculator.isValidPieceSize(64 * 1024));
        assertTrue(calculator.isValidPieceSize(1024 * 1024));
        
        // Not a power of 2
        assertFalse(calculator.isValidPieceSize(100 * 1024));
        
        // Too small
        assertFalse(calculator.isValidPieceSize(8 * 1024));
        
        // Too large
        assertFalse(calculator.isValidPieceSize(32 * 1024 * 1024));
    }

    @Test
    void roundToValidPieceSize() {
        assertEquals(32 * 1024, calculator.roundToValidPieceSize(30 * 1024));
        assertEquals(64 * 1024, calculator.roundToValidPieceSize(50 * 1024));
        assertEquals(16 * 1024, calculator.roundToValidPieceSize(10 * 1024));
        assertEquals(16 * 1024, calculator.roundToValidPieceSize(1));
        assertEquals(16 * 1024 * 1024, calculator.roundToValidPieceSize(Integer.MAX_VALUE));
    }

    @Test
    void estimateTorrentFileSize() {
        // 100 pieces * 20 bytes + 500 overhead
        assertEquals(2500, calculator.estimateTorrentFileSize(100));
    }

    @Test
    void dynamicPieceSizeAdjustsForPieceCount() {
        // Very large file should have larger pieces to keep piece count reasonable
        int dynamicSize = calculator.calculateDynamicPieceSize(100_000_000_000L);
        assertTrue(dynamicSize >= 1024 * 1024);
    }
}
