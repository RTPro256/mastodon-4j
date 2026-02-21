package org.joinmastodon.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JobService.
 * Tests job queue management and execution.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setId(1L);
        testJob.setQueue("default");
        testJob.setPayload("{\"task\": \"test\"}");
        testJob.setRunAt(Instant.now());
        testJob.setAttempts(0);
    }

    @Nested
    @DisplayName("Enqueue job")
    class EnqueueTests {

        @Test
        @DisplayName("Enqueues job with immediate run time")
        void enqueuesJobWithImmediateRunTime() {
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
                Job job = inv.getArgument(0);
                job.setId(1L);
                return job;
            });
            
            Job result = jobService.enqueue("default", "{\"task\": \"test\"}", null);
            
            assertThat(result).isNotNull();
            assertThat(result.getQueue()).isEqualTo("default");
            assertThat(result.getPayload()).isEqualTo("{\"task\": \"test\"}");
            assertThat(result.getRunAt()).isNotNull();
            verify(jobRepository).save(any(Job.class));
        }

        @Test
        @DisplayName("Enqueues job with scheduled run time")
        void enqueuesJobWithScheduledRunTime() {
            Instant scheduledTime = Instant.now().plus(Duration.ofHours(1));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
                Job job = inv.getArgument(0);
                job.setId(1L);
                return job;
            });
            
            Job result = jobService.enqueue("default", "{\"task\": \"test\"}", scheduledTime);
            
            assertThat(result.getRunAt()).isEqualTo(scheduledTime);
        }

        @Test
        @DisplayName("Enqueues job to specific queue")
        void enqueuesJobToSpecificQueue() {
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            Job result = jobService.enqueue("priority", "{\"task\": \"urgent\"}", null);
            
            assertThat(result.getQueue()).isEqualTo("priority");
        }
    }

    @Nested
    @DisplayName("Claim next jobs")
    class ClaimNextJobsTests {

        @Test
        @DisplayName("Claims available jobs")
        void claimsAvailableJobs() {
            Job job1 = createJob(1L, "default");
            Job job2 = createJob(2L, "default");
            when(jobRepository.claimNextJobs(any(), any(), any(), any(Integer.class)))
                    .thenReturn(List.of(job1, job2));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            List<Job> result = jobService.claimNextJobs("default", "worker-1", 10, Duration.ofMinutes(5));
            
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Sets lock information on claimed jobs")
        void setsLockInformationOnClaimedJobs() {
            when(jobRepository.claimNextJobs(any(), any(), any(), any(Integer.class)))
                    .thenReturn(List.of(testJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            List<Job> result = jobService.claimNextJobs("default", "worker-1", 10, Duration.ofMinutes(5));
            
            assertThat(result.get(0).getLockedBy()).isEqualTo("worker-1");
            assertThat(result.get(0).getLockedAt()).isNotNull();
        }

        @Test
        @DisplayName("Increments attempt count on claimed jobs")
        void incrementsAttemptCountOnClaimedJobs() {
            testJob.setAttempts(2);
            when(jobRepository.claimNextJobs(any(), any(), any(), any(Integer.class)))
                    .thenReturn(List.of(testJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            List<Job> result = jobService.claimNextJobs("default", "worker-1", 10, Duration.ofMinutes(5));
            
            assertThat(result.get(0).getAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("Returns empty list when no jobs available")
        void returnsEmptyListWhenNoJobsAvailable() {
            when(jobRepository.claimNextJobs(any(), any(), any(), any(Integer.class)))
                    .thenReturn(List.of());
            
            List<Job> result = jobService.claimNextJobs("default", "worker-1", 10, Duration.ofMinutes(5));
            
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Uses default lock timeout when not specified")
        void usesDefaultLockTimeoutWhenNotSpecified() {
            when(jobRepository.claimNextJobs(any(), any(), any(), any(Integer.class)))
                    .thenReturn(List.of(testJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            jobService.claimNextJobs("default", "worker-1", 10, null);
            
            // Should use default 5 minute timeout
            verify(jobRepository).claimNextJobs(any(), any(), any(), any(Integer.class));
        }
    }

    @Nested
    @DisplayName("Mark success")
    class MarkSuccessTests {

        @Test
        @DisplayName("Deletes job on success")
        void deletesJobOnSuccess() {
            jobService.markSuccess(testJob);
            
            verify(jobRepository).delete(testJob);
        }
    }

    @Nested
    @DisplayName("Mark failure")
    class MarkFailureTests {

        @Test
        @DisplayName("Records error and clears lock")
        void recordsErrorAndClearsLock() {
            testJob.setLockedBy("worker-1");
            testJob.setLockedAt(Instant.now());
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            Job result = jobService.markFailure(testJob, "Connection timeout", Duration.ofMinutes(1));
            
            assertThat(result.getLastError()).isEqualTo("Connection timeout");
            assertThat(result.getLockedBy()).isNull();
            assertThat(result.getLockedAt()).isNull();
        }

        @Test
        @DisplayName("Schedules retry with backoff")
        void schedulesRetryWithBackoff() {
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            Job result = jobService.markFailure(testJob, "Error", Duration.ofMinutes(5));
            
            assertThat(result.getRunAt()).isAfter(Instant.now().plus(Duration.ofMinutes(4)));
        }

        @Test
        @DisplayName("Uses default backoff when not specified")
        void usesDefaultBackoffWhenNotSpecified() {
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));
            
            Job result = jobService.markFailure(testJob, "Error", null);
            
            // Default backoff is 30 seconds
            assertThat(result.getRunAt()).isAfter(Instant.now().plus(Duration.ofSeconds(25)));
            assertThat(result.getRunAt()).isBefore(Instant.now().plus(Duration.ofSeconds(35)));
        }
    }

    @Nested
    @DisplayName("Job entity tests")
    class JobEntityTests {

        @Test
        @DisplayName("New job has zero attempts")
        void newJobHasZeroAttempts() {
            Job job = new Job();
            
            assertThat(job.getAttempts()).isEqualTo(0);
        }

        @Test
        @DisplayName("Job can be locked")
        void jobCanBeLocked() {
            Job job = new Job();
            Instant lockTime = Instant.now();
            
            job.setLockedAt(lockTime);
            job.setLockedBy("worker-1");
            
            assertThat(job.getLockedAt()).isEqualTo(lockTime);
            assertThat(job.getLockedBy()).isEqualTo("worker-1");
        }
    }

    // Helper methods

    private Job createJob(Long id, String queue) {
        Job job = new Job();
        job.setId(id);
        job.setQueue(queue);
        job.setPayload("{\"id\": " + id + "}");
        job.setRunAt(Instant.now());
        job.setAttempts(0);
        return job;
    }
}
