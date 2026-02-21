package org.joinmastodon.cluster.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Distributed task queue for the cluster.
 * Manages task submission, assignment, and completion tracking.
 */
@Component
public class TaskQueue {

    private static final Logger log = LoggerFactory.getLogger(TaskQueue.class);

    private final PriorityBlockingQueue<DistributedTask> pendingTasks;
    private final Map<String, DistributedTask> assignedTasks;
    private final Map<String, DistributedTask> completedTasks;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TaskQueue() {
        this.pendingTasks = new PriorityBlockingQueue<>(100, 
                Comparator.comparingInt(DistributedTask::getPriority).reversed()
                        .thenComparing(DistributedTask::getCreatedAt));
        this.assignedTasks = new ConcurrentHashMap<>();
        this.completedTasks = new ConcurrentHashMap<>();
    }

    /**
     * Start the task queue.
     */
    public void start() {
        running.set(true);
        log.info("Task queue started");
    }

    /**
     * Stop the task queue.
     */
    public void stop() {
        running.set(false);
        log.info("Task queue stopped");
    }

    /**
     * Submit a new task to the queue.
     */
    public String submit(DistributedTask task) {
        if (!running.get()) {
            throw new IllegalStateException("Task queue is not running");
        }

        log.debug("Submitting task: {} (type: {})", task.getId(), task.getType());
        pendingTasks.offer(task);
        return task.getId();
    }

    /**
     * Submit multiple tasks to the queue.
     */
    public List<String> submitAll(List<DistributedTask> tasks) {
        List<String> taskIds = new ArrayList<>();
        for (DistributedTask task : tasks) {
            taskIds.add(submit(task));
        }
        return taskIds;
    }

    /**
     * Get the next pending task for a node.
     */
    public Optional<DistributedTask> poll(String nodeId) {
        if (!running.get()) {
            return Optional.empty();
        }

        DistributedTask task = pendingTasks.poll();
        if (task == null) {
            return Optional.empty();
        }

        // Check if task has expired
        if (task.isExpired()) {
            log.debug("Task {} has expired, skipping", task.getId());
            return poll(nodeId); // Try next task
        }

        // Assign to node
        DistributedTask assigned = task.withAssignment(nodeId);
        assignedTasks.put(assigned.getId(), assigned);
        log.debug("Assigned task {} to node {}", task.getId(), nodeId);
        return Optional.of(assigned);
    }

    /**
     * Poll with timeout.
     */
    public Optional<DistributedTask> poll(String nodeId, long timeout, TimeUnit unit) 
            throws InterruptedException {
        if (!running.get()) {
            return Optional.empty();
        }

        DistributedTask task = pendingTasks.poll(timeout, unit);
        if (task == null) {
            return Optional.empty();
        }

        // Check if task has expired
        if (task.isExpired()) {
            log.debug("Task {} has expired, skipping", task.getId());
            return poll(nodeId); // Try next task
        }

        // Assign to node
        DistributedTask assigned = task.withAssignment(nodeId);
        assignedTasks.put(assigned.getId(), assigned);
        log.debug("Assigned task {} to node {}", task.getId(), nodeId);
        return Optional.of(assigned);
    }

    /**
     * Mark a task as completed.
     */
    public void complete(String taskId) {
        DistributedTask task = assignedTasks.remove(taskId);
        if (task != null) {
            completedTasks.put(taskId, task.withStatus(DistributedTask.TaskStatus.COMPLETED));
            log.debug("Task {} completed", taskId);
        }
    }

    /**
     * Mark a task as failed.
     */
    public void fail(String taskId, boolean retry) {
        DistributedTask task = assignedTasks.remove(taskId);
        if (task != null) {
            if (retry && task.canRetry()) {
                // Re-queue for retry
                DistributedTask retried = task.withRetry();
                pendingTasks.offer(retried);
                log.debug("Task {} re-queued for retry (attempt {})", taskId, retried.getRetryCount());
            } else {
                completedTasks.put(taskId, task.withStatus(DistributedTask.TaskStatus.FAILED));
                log.warn("Task {} failed permanently", taskId);
            }
        }
    }

    /**
     * Cancel a task.
     */
    public boolean cancel(String taskId) {
        // Check pending queue
        boolean removed = pendingTasks.removeIf(t -> t.getId().equals(taskId));
        if (removed) {
            log.debug("Cancelled pending task {}", taskId);
            return true;
        }

        // Check assigned tasks
        DistributedTask task = assignedTasks.remove(taskId);
        if (task != null) {
            completedTasks.put(taskId, task.withStatus(DistributedTask.TaskStatus.CANCELLED));
            log.debug("Cancelled assigned task {}", taskId);
            return true;
        }

        return false;
    }

    /**
     * Reassign tasks from a failed node.
     */
    public int reassignFromNode(String nodeId) {
        int reassigned = 0;
        Iterator<Map.Entry<String, DistributedTask>> it = assignedTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DistributedTask> entry = it.next();
            DistributedTask task = entry.getValue();
            if (nodeId.equals(task.getAssignedNodeId())) {
                it.remove();
                if (task.canRetry()) {
                    pendingTasks.offer(task.withRetry());
                    reassigned++;
                } else {
                    completedTasks.put(task.getId(), task.withStatus(DistributedTask.TaskStatus.FAILED));
                }
            }
        }
        log.info("Reassigned {} tasks from failed node {}", reassigned, nodeId);
        return reassigned;
    }

    /**
     * Get queue statistics.
     */
    public QueueStats getStats() {
        return new QueueStats(
                pendingTasks.size(),
                assignedTasks.size(),
                (int) completedTasks.values().stream()
                        .filter(t -> t.getStatus() == DistributedTask.TaskStatus.COMPLETED)
                        .count(),
                (int) completedTasks.values().stream()
                        .filter(t -> t.getStatus() == DistributedTask.TaskStatus.FAILED)
                        .count()
        );
    }

    /**
     * Get a task by ID.
     */
    public Optional<DistributedTask> getTask(String taskId) {
        // Check all locations
        Optional<DistributedTask> task = Optional.ofNullable(assignedTasks.get(taskId));
        if (task.isPresent()) return task;

        task = Optional.ofNullable(completedTasks.get(taskId));
        if (task.isPresent()) return task;

        return pendingTasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
    }

    /**
     * Clean up old completed tasks.
     */
    public void cleanup(Instant olderThan) {
        completedTasks.entrySet().removeIf(entry -> 
                entry.getValue().getCreatedAt().isBefore(olderThan));
    }

    /**
     * Queue statistics.
     */
    public static class QueueStats {
        private final int pending;
        private final int assigned;
        private final int completed;
        private final int failed;

        public QueueStats(int pending, int assigned, int completed, int failed) {
            this.pending = pending;
            this.assigned = assigned;
            this.completed = completed;
            this.failed = failed;
        }

        public int getPending() {
            return pending;
        }

        public int getAssigned() {
            return assigned;
        }

        public int getCompleted() {
            return completed;
        }

        public int getFailed() {
            return failed;
        }

        public int getTotal() {
            return pending + assigned + completed + failed;
        }
    }
}
