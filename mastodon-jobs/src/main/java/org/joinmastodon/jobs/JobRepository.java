package org.joinmastodon.jobs;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query(value = """
            select * from jobs
            where queue = :queue
              and run_at <= :now
              and (locked_at is null or locked_at < :staleBefore)
              and attempts < max_attempts
            order by run_at asc
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<Job> claimNextJobs(
            @Param("queue") String queue,
            @Param("now") Instant now,
            @Param("staleBefore") Instant staleBefore,
            @Param("limit") int limit);
}
