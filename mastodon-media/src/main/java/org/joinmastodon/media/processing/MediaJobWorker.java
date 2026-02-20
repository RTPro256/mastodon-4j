package org.joinmastodon.media.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.joinmastodon.jobs.Job;
import org.joinmastodon.jobs.JobService;
import org.joinmastodon.media.config.MediaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mastodon.media.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MediaJobWorker {
    private final JobService jobService;
    private final MediaProcessingService mediaProcessingService;
    private final MediaCleanupService mediaCleanupService;
    private final ObjectMapper objectMapper;
    private final MediaProperties properties;
    private final String workerId = "media-worker-" + UUID.randomUUID();

    public MediaJobWorker(JobService jobService,
                          MediaProcessingService mediaProcessingService,
                          MediaCleanupService mediaCleanupService,
                          ObjectMapper objectMapper,
                          MediaProperties properties) {
        this.jobService = jobService;
        this.mediaProcessingService = mediaProcessingService;
        this.mediaCleanupService = mediaCleanupService;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${mastodon.media.processing.poll-interval-ms:5000}")
    public void processMediaJobs() {
        List<Job> jobs = jobService.claimNextJobs(
                MediaJobQueues.MEDIA_PROCESSING,
                workerId,
                properties.getProcessingBatchSize(),
                properties.getProcessingLockTimeout());
        for (Job job : jobs) {
            try {
                MediaJobPayload payload = objectMapper.readValue(job.getPayload(), MediaJobPayload.class);
                if (payload.mediaId() == null) {
                    throw new IllegalStateException("Missing mediaId");
                }
                mediaProcessingService.process(payload.mediaId());
                jobService.markSuccess(job);
            } catch (Exception ex) {
                jobService.markFailure(job, ex.getMessage(), backoff(job.getAttempts()));
            }
        }
    }

    @Scheduled(fixedDelayString = "${mastodon.media.cleanup.poll-interval-ms:600000}")
    public void processCleanupJobs() {
        List<Job> jobs = jobService.claimNextJobs(
                MediaJobQueues.MEDIA_CLEANUP,
                workerId,
                1,
                properties.getProcessingLockTimeout());
        for (Job job : jobs) {
            try {
                mediaCleanupService.cleanupOrphaned();
                jobService.markSuccess(job);
            } catch (Exception ex) {
                jobService.markFailure(job, ex.getMessage(), backoff(job.getAttempts()));
            }
        }
    }

    private Duration backoff(int attempts) {
        int capped = Math.max(1, Math.min(attempts, 6));
        long seconds = (long) Math.pow(2, capped);
        return Duration.ofSeconds(Math.min(seconds, 300));
    }
}
