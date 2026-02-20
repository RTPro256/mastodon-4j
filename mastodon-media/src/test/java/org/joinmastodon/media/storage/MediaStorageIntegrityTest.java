package org.joinmastodon.media.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for media storage integrity.
 * Verifies file storage and retrieval operations.
 */
@DisplayName("Media Storage Integrity Tests")
class MediaStorageIntegrityTest {

    @TempDir
    Path tempDir;

    private MediaStorage mediaStorage;

    @BeforeEach
    void setUp() {
        mediaStorage = new LocalFileSystemMediaStorage(
                tempDir.toString(),
                "https://example.com/media"
        );
    }

    @Nested
    @DisplayName("File Storage")
    class FileStorageTests {

        @Test
        @DisplayName("Stores file and returns key")
        void storesFileAndReturnsKey() throws IOException {
            byte[] content = "test image content".getBytes();

            String key = mediaStorage.save("images/test.png", content);

            assertThat(key).isNotNull();
            assertThat(key).isEqualTo("images/test.png");
        }

        @Test
        @DisplayName("Creates directory structure")
        void createsDirectoryStructure() throws IOException {
            byte[] content = "test content".getBytes();

            mediaStorage.save("images/2024/02/test.png", content);

            Path expectedDir = tempDir.resolve("images/2024/02");
            assertThat(Files.exists(expectedDir)).isTrue();
            assertThat(Files.isDirectory(expectedDir)).isTrue();
        }

        @Test
        @DisplayName("Stores file with correct content")
        void storesFileWithCorrectContent() throws IOException {
            byte[] content = "original content for verification".getBytes();

            String key = mediaStorage.save("test/file.dat", content);

            byte[] loaded = mediaStorage.load(key);
            assertThat(loaded).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("File Retrieval")
    class FileRetrievalTests {

        @Test
        @DisplayName("Retrieves existing file")
        void retrievesExistingFile() throws IOException {
            byte[] content = "content to retrieve".getBytes();
            String key = mediaStorage.save("retrieve/test.txt", content);

            byte[] loaded = mediaStorage.load(key);

            assertThat(loaded).isEqualTo(content);
        }

        @Test
        @DisplayName("Throws for non-existent file")
        void throwsForNonExistentFile() {
            assertThatThrownBy(() -> mediaStorage.load("nonexistent/file.txt"))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("File Deletion")
    class FileDeletionTests {

        @Test
        @DisplayName("Deletes existing file")
        void deletesExistingFile() throws IOException {
            byte[] content = "content to delete".getBytes();
            String key = mediaStorage.save("delete/test.txt", content);

            boolean deleted = mediaStorage.delete(key);

            assertThat(deleted).isTrue();
            assertThat(Files.exists(tempDir.resolve(key))).isFalse();
        }

        @Test
        @DisplayName("Returns false for non-existent file")
        void returnsFalseForNonExistentFile() throws IOException {
            boolean deleted = mediaStorage.delete("nonexistent/file.txt");

            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("URL Resolution")
    class UrlResolutionTests {

        @Test
        @DisplayName("Resolves URL for stored file")
        void resolvesUrlForStoredFile() throws IOException {
            byte[] content = "test content".getBytes();
            String key = mediaStorage.save("images/test.png", content);

            String url = mediaStorage.resolveUrl(key);

            assertThat(url).isEqualTo("https://example.com/media/images/test.png");
        }

        @Test
        @DisplayName("Resolves path for stored file")
        void resolvesPathForStoredFile() throws IOException {
            byte[] content = "test content".getBytes();
            String key = mediaStorage.save("images/test.png", content);

            Path path = mediaStorage.resolvePath(key);

            assertThat(path).isEqualTo(tempDir.resolve("images/test.png"));
        }
    }

    @Nested
    @DisplayName("Content Type Handling")
    class ContentTypeTests {

        @Test
        @DisplayName("Stores image files")
        void storesImageFiles() throws IOException {
            byte[] content = "fake image data".getBytes();

            String key = mediaStorage.save("images/photo.jpg", content);

            assertThat(key).isNotNull();
            assertThat(mediaStorage.load(key)).isEqualTo(content);
        }

        @Test
        @DisplayName("Stores video files")
        void storesVideoFiles() throws IOException {
            byte[] content = "fake video data".getBytes();

            String key = mediaStorage.save("videos/video.mp4", content);

            assertThat(key).isNotNull();
            assertThat(mediaStorage.load(key)).isEqualTo(content);
        }

        @Test
        @DisplayName("Stores audio files")
        void storesAudioFiles() throws IOException {
            byte[] content = "fake audio data".getBytes();

            String key = mediaStorage.save("audio/audio.mp3", content);

            assertThat(key).isNotNull();
            assertThat(mediaStorage.load(key)).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("Large File Handling")
    class LargeFileTests {

        @Test
        @DisplayName("Handles large files")
        void handlesLargeFiles() throws IOException {
            // Create a 1MB test file
            byte[] content = new byte[1024 * 1024];
            for (int i = 0; i < content.length; i++) {
                content[i] = (byte) (i % 256);
            }

            String key = mediaStorage.save("large/file.bin", content);

            byte[] loaded = mediaStorage.load(key);
            assertThat(loaded.length).isEqualTo(content.length);
            assertThat(loaded).isEqualTo(content);
        }
    }
}
