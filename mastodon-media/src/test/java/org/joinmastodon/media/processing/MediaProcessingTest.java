package org.joinmastodon.media.processing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.joinmastodon.media.config.MediaProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for media processing functionality.
 */
@DisplayName("Media Processing Tests")
class MediaProcessingTest {

    private MediaProperties mediaProperties;

    @BeforeEach
    void setUp() {
        mediaProperties = new MediaProperties();
    }

    @Nested
    @DisplayName("Allowed Media Types")
    class AllowedMediaTypesTests {

        @Test
        @DisplayName("Default allowed types include common formats")
        void defaultAllowedTypesIncludeCommonFormats() {
            List<String> allowedTypes = mediaProperties.getAllowedTypes();

            assertThat(allowedTypes).contains(
                    "image/jpeg",
                    "image/png",
                    "image/gif",
                    "video/mp4",
                    "video/webm",
                    "audio/mpeg",
                    "audio/ogg"
            );
        }

        @Test
        @DisplayName("Allowed types are configurable")
        void allowedTypesAreConfigurable() {
            List<String> customTypes = List.of("image/jpeg", "image/png");
            
            mediaProperties.setAllowedTypes(customTypes);
            
            assertThat(mediaProperties.getAllowedTypes())
                    .containsExactlyElementsOf(customTypes);
        }
    }

    @Nested
    @DisplayName("Upload Size Limits")
    class UploadSizeLimitsTests {

        @Test
        @DisplayName("Default max upload size is 10MB")
        void defaultMaxUploadSizeIs10MB() {
            assertThat(mediaProperties.getMaxUploadSize().toMegabytes()).isEqualTo(10);
        }

        @Test
        @DisplayName("Max upload size is configurable")
        void maxUploadSizeIsConfigurable() {
            mediaProperties.setMaxUploadSize(
                    org.springframework.util.unit.DataSize.ofMegabytes(50)
            );
            
            assertThat(mediaProperties.getMaxUploadSize().toMegabytes()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Preview Settings")
    class PreviewSettingsTests {

        @Test
        @DisplayName("Default preview dimensions are set")
        void defaultPreviewDimensionsAreSet() {
            assertThat(mediaProperties.getPreviewMaxWidth()).isEqualTo(400);
            assertThat(mediaProperties.getPreviewMaxHeight()).isEqualTo(400);
        }

        @Test
        @DisplayName("Preview dimensions are configurable")
        void previewDimensionsAreConfigurable() {
            mediaProperties.setPreviewMaxWidth(800);
            mediaProperties.setPreviewMaxHeight(600);
            
            assertThat(mediaProperties.getPreviewMaxWidth()).isEqualTo(800);
            assertThat(mediaProperties.getPreviewMaxHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("Media Kind Detection")
    class MediaKindDetectionTests {

        @Test
        @DisplayName("Detects image kind")
        void detectsImageKind() {
            assertThat(MediaKind.fromContentType("image/jpeg")).isEqualTo(MediaKind.IMAGE);
            assertThat(MediaKind.fromContentType("image/png")).isEqualTo(MediaKind.IMAGE);
            assertThat(MediaKind.fromContentType("image/gif")).isEqualTo(MediaKind.IMAGE);
            assertThat(MediaKind.fromContentType("image/webp")).isEqualTo(MediaKind.IMAGE);
        }

        @Test
        @DisplayName("Detects video kind")
        void detectsVideoKind() {
            assertThat(MediaKind.fromContentType("video/mp4")).isEqualTo(MediaKind.VIDEO);
            assertThat(MediaKind.fromContentType("video/webm")).isEqualTo(MediaKind.VIDEO);
            assertThat(MediaKind.fromContentType("video/quicktime")).isEqualTo(MediaKind.VIDEO);
        }

        @Test
        @DisplayName("Detects audio kind")
        void detectsAudioKind() {
            assertThat(MediaKind.fromContentType("audio/mpeg")).isEqualTo(MediaKind.AUDIO);
            assertThat(MediaKind.fromContentType("audio/ogg")).isEqualTo(MediaKind.AUDIO);
            assertThat(MediaKind.fromContentType("audio/wav")).isEqualTo(MediaKind.AUDIO);
        }

        @Test
        @DisplayName("Returns unknown for unrecognized types")
        void returnsUnknownForUnrecognizedTypes() {
            assertThat(MediaKind.fromContentType("application/pdf"))
                    .isEqualTo(MediaKind.UNKNOWN);
            assertThat(MediaKind.fromContentType("text/plain"))
                    .isEqualTo(MediaKind.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("Processing Configuration")
    class ProcessingConfigurationTests {

        @Test
        @DisplayName("Processing batch size is configurable")
        void processingBatchSizeIsConfigurable() {
            mediaProperties.setProcessingBatchSize(10);
            
            assertThat(mediaProperties.getProcessingBatchSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Processing poll interval is configurable")
        void processingPollIntervalIsConfigurable() {
            java.time.Duration interval = java.time.Duration.ofSeconds(10);
            mediaProperties.setProcessingPollInterval(interval);
            
            assertThat(mediaProperties.getProcessingPollInterval()).isEqualTo(interval);
        }

        @Test
        @DisplayName("Processing lock timeout is configurable")
        void processingLockTimeoutIsConfigurable() {
            java.time.Duration timeout = java.time.Duration.ofMinutes(10);
            mediaProperties.setProcessingLockTimeout(timeout);
            
            assertThat(mediaProperties.getProcessingLockTimeout()).isEqualTo(timeout);
        }
    }

    @Nested
    @DisplayName("Transcoding Settings")
    class TranscodingSettingsTests {

        @Test
        @DisplayName("Transcoding is disabled by default")
        void transcodingIsDisabledByDefault() {
            assertThat(mediaProperties.isTranscodeEnabled()).isFalse();
        }

        @Test
        @DisplayName("Transcoding can be enabled")
        void transcodingCanBeEnabled() {
            mediaProperties.setTranscodeEnabled(true);
            
            assertThat(mediaProperties.isTranscodeEnabled()).isTrue();
        }

        @Test
        @DisplayName("FFmpeg path is configurable")
        void ffmpegPathIsConfigurable() {
            mediaProperties.setFfmpegPath("/usr/local/bin/ffmpeg");
            
            assertThat(mediaProperties.getFfmpegPath()).isEqualTo("/usr/local/bin/ffmpeg");
        }

        @Test
        @DisplayName("FFprobe path is configurable")
        void ffprobePathIsConfigurable() {
            mediaProperties.setFfprobePath("/usr/local/bin/ffprobe");
            
            assertThat(mediaProperties.getFfprobePath()).isEqualTo("/usr/local/bin/ffprobe");
        }
    }
}
