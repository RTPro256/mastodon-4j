package org.joinmastodon.media.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.joinmastodon.jobs.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MediaCleanupScheduler {
    private final JobService jobService;
    private final ObjectMapper objectMapper;

    public MediaCleanupScheduler(JobService jobService, ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "${mastodon.media.cleanup.cron:0 0 3 * * *}")
    public void enqueueCleanup() {
        String payload = "{}";
        try {
            payload = objectMapper.writeValueAsString(Map.of("scheduledAt", Instant.now().toString()));
        } catch (JsonProcessingException ignored) {
        }
        jobService.enqueue(MediaJobQueues.MEDIA_CLEANUP, payload, Instant.now());
    }
}
