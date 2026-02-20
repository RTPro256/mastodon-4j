package org.joinmastodon.media.processing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.joinmastodon.media.config.MediaProperties;
import org.joinmastodon.media.storage.LocalFileSystemMediaStorage;
import org.joinmastodon.media.storage.MediaStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for orphaned media cleanup.
 */
@DisplayName("Media Cleanup Tests")
class MediaCleanupTest {

    @TempDir
    Path tempDir;

    private MediaStorage mediaStorage;
    private MediaProperties mediaProperties;

    @BeforeEach
    void setUp() {
        mediaProperties = new MediaProperties();
        mediaProperties.setStoragePath(tempDir.toString());
        mediaProperties.setCleanupRetention(Duration.ofDays(7));
        
        mediaStorage = new LocalFileSystemMediaStorage(
                tempDir.toString(),
                "https://example.com/media"
        );
    }

    @Nested
    @DisplayName("Retention Policy")
    class RetentionPolicyTests {

        @Test
        @DisplayName("Retention duration is configurable")
        void retentionDurationIsConfigurable() {
            Duration expected = Duration.ofDays(14);
            mediaProperties.setCleanupRetention(expected);
            
            assertThat(mediaProperties.getCleanupRetention()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Default retention is 7 days")
        void defaultRetentionIs7Days() {
            MediaProperties props = new MediaProperties();
            assertThat(props.getCleanupRetention()).isEqualTo(Duration.ofDays(7));
        }
    }

    @Nested
    @DisplayName("File Age Detection")
    class FileAgeDetectionTests {

        @Test
        @DisplayName("Can determine file age")
        void canDetermineFileAge() throws IOException {
            byte[] content = "test content".getBytes();
            String key = mediaStorage.save("test/file.txt", content);
            Path filePath = mediaStorage.resolvePath(key);
            
            Instant now = Instant.now();
            Instant fileTime = Files.getLastModifiedTime(filePath).toInstant();
            
            // File should be created within the last second
            Duration age = Duration.between(fileTime, now);
            assertThat(age).isLessThan(Duration.ofSeconds(1));
        }

        @Test
        @DisplayName("Can identify old files")
        void canIdentifyOldFiles() throws IOException {
            byte[] content = "test content".getBytes();
            String key = mediaStorage.save("test/old-file.txt", content);
            Path filePath = mediaStorage.resolvePath(key);
            
            // Set modification time to 10 days ago
            Instant oldTime = Instant.now().minus(Duration.ofDays(10));
            Files.setLastModifiedTime(filePath, java.nio.file.attribute.FileTime.from(oldTime));
            
            Instant fileTime = Files.getLastModifiedTime(filePath).toInstant();
            Duration age = Duration.between(fileTime, Instant.now());
            
            assertThat(age).isGreaterThan(Duration.ofDays(7));
        }
    }

    @Nested
    @DisplayName("Cleanup Operations")
    class CleanupOperationsTests {

        @Test
        @DisplayName("Can delete single file")
        void canDeleteSingleFile() throws IOException {
            byte[] content = "content to delete".getBytes();
            String key = mediaStorage.save("cleanup/test.txt", content);
            
            boolean deleted = mediaStorage.delete(key);
            
            assertThat(deleted).isTrue();
            assertThat(Files.exists(mediaStorage.resolvePath(key))).isFalse();
        }

        @Test
        @DisplayName("Can identify files for cleanup")
        void canIdentifyFilesForCleanup() throws IOException {
            // Create multiple files
            mediaStorage.save("cleanup/file1.txt", "content1".getBytes());
            mediaStorage.save("cleanup/file2.txt", "content2".getBytes());
            mediaStorage.save("cleanup/file3.txt", "content3".getBytes());
            
            // Count files in cleanup directory
            long fileCount = Files.list(tempDir.resolve("cleanup"))
                    .filter(Files::isRegularFile)
                    .count();
            
            assertThat(fileCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Batch Processing")
    class BatchProcessingTests {

        @Test
        @DisplayName("Batch size is configurable")
        void batchSizeIsConfigurable() {
            int expectedBatchSize = 50;
            mediaProperties.setCleanupBatchSize(expectedBatchSize);
            
            assertThat(mediaProperties.getCleanupBatchSize()).isEqualTo(expectedBatchSize);
        }

        @Test
        @DisplayName("Default batch size is 100")
        void defaultBatchSizeIs100() {
            MediaProperties props = new MediaProperties();
            assertThat(props.getCleanupBatchSize()).isEqualTo(100);
        }
    }
}
