package org.joinmastodon.media.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "mastodon.media")
public class MediaProperties {
    private DataSize maxUploadSize = DataSize.ofMegabytes(10);
    private List<String> allowedTypes = new ArrayList<>(List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "video/mp4",
            "video/webm",
            "audio/mpeg",
            "audio/ogg"
    ));
    private String storagePath = "data/media";
    private String baseUrl;
    private String ffmpegPath = "ffmpeg";
    private String ffprobePath = "ffprobe";
    private boolean transcodeEnabled = false;
    private int previewMaxWidth = 400;
    private int previewMaxHeight = 400;
    private Duration cleanupRetention = Duration.ofDays(7);
    private int cleanupBatchSize = 100;
    private Duration processingLockTimeout = Duration.ofMinutes(5);
    private int processingBatchSize = 5;
    private Duration processingPollInterval = Duration.ofSeconds(5);

    public DataSize getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(DataSize maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getFfprobePath() {
        return ffprobePath;
    }

    public void setFfprobePath(String ffprobePath) {
        this.ffprobePath = ffprobePath;
    }

    public boolean isTranscodeEnabled() {
        return transcodeEnabled;
    }

    public void setTranscodeEnabled(boolean transcodeEnabled) {
        this.transcodeEnabled = transcodeEnabled;
    }

    public int getPreviewMaxWidth() {
        return previewMaxWidth;
    }

    public void setPreviewMaxWidth(int previewMaxWidth) {
        this.previewMaxWidth = previewMaxWidth;
    }

    public int getPreviewMaxHeight() {
        return previewMaxHeight;
    }

    public void setPreviewMaxHeight(int previewMaxHeight) {
        this.previewMaxHeight = previewMaxHeight;
    }

    public Duration getCleanupRetention() {
        return cleanupRetention;
    }

    public void setCleanupRetention(Duration cleanupRetention) {
        this.cleanupRetention = cleanupRetention;
    }

    public int getCleanupBatchSize() {
        return cleanupBatchSize;
    }

    public void setCleanupBatchSize(int cleanupBatchSize) {
        this.cleanupBatchSize = cleanupBatchSize;
    }

    public Duration getProcessingLockTimeout() {
        return processingLockTimeout;
    }

    public void setProcessingLockTimeout(Duration processingLockTimeout) {
        this.processingLockTimeout = processingLockTimeout;
    }

    public int getProcessingBatchSize() {
        return processingBatchSize;
    }

    public void setProcessingBatchSize(int processingBatchSize) {
        this.processingBatchSize = processingBatchSize;
    }

    public Duration getProcessingPollInterval() {
        return processingPollInterval;
    }

    public void setProcessingPollInterval(Duration processingPollInterval) {
        this.processingPollInterval = processingPollInterval;
    }
}
