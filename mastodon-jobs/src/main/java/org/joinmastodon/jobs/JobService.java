package org.joinmastodon.jobs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job enqueue(String queue, String payload, Instant runAt) {
        Job job = new Job();
        job.setQueue(queue);
        job.setPayload(payload);
        job.setRunAt(runAt == null ? Instant.now() : runAt);
        return jobRepository.save(job);
    }

    @Transactional
    public List<Job> claimNextJobs(String queue, String workerId, int limit, Duration lockTimeout) {
        Instant now = Instant.now();
        Instant staleBefore = now.minus(lockTimeout == null ? Duration.ofMinutes(5) : lockTimeout);
        List<Job> jobs = jobRepository.claimNextJobs(queue, now, staleBefore, limit);
        for (Job job : jobs) {
            job.setLockedAt(now);
            job.setLockedBy(workerId);
            job.setAttempts(job.getAttempts() + 1);
            jobRepository.save(job);
        }
        return jobs;
    }

    @Transactional
    public void markSuccess(Job job) {
        jobRepository.delete(job);
    }

    @Transactional
    public Job markFailure(Job job, String error, Duration backoff) {
        job.setLastError(error);
        job.setLockedAt(null);
        job.setLockedBy(null);
        Duration delay = backoff == null ? Duration.ofSeconds(30) : backoff;
        job.setRunAt(Instant.now().plus(delay));
        return jobRepository.save(job);
    }
}
