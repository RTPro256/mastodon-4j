package org.joinmastodon.core.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of configuration validation containing all issues found.
 */
public class ValidationResult {
    
    private Instant timestamp;
    private boolean valid;
    private int errorCount;
    private int warningCount;
    private int infoCount;
    private List<ValidationIssue> issues;
    private String category;
    
    public ValidationResult() {
        this.timestamp = Instant.now();
        this.issues = new ArrayList<>();
    }
    
    /**
     * Create a validation result for a specific category.
     */
    public static ValidationResult forCategory(String category) {
        ValidationResult result = new ValidationResult();
        result.category = category;
        return result;
    }
    
    /**
     * Add an issue to this result.
     */
    public ValidationResult addIssue(ValidationIssue issue) {
        issues.add(issue);
        switch (issue.getSeverity()) {
            case ERROR -> errorCount++;
            case WARNING -> warningCount++;
            case INFO -> infoCount++;
        }
        updateValidStatus();
        return this;
    }
    
    /**
     * Add multiple issues to this result.
     */
    public ValidationResult addIssues(List<ValidationIssue> newIssues) {
        for (ValidationIssue issue : newIssues) {
            addIssue(issue);
        }
        return this;
    }
    
    /**
     * Merge another validation result into this one.
     */
    public ValidationResult merge(ValidationResult other) {
        for (ValidationIssue issue : other.getIssues()) {
            addIssue(issue);
        }
        return this;
    }
    
    private void updateValidStatus() {
        this.valid = errorCount == 0;
    }
    
    /**
     * Get all issues sorted by severity (errors first).
     */
    public List<ValidationIssue> getSortedIssues() {
        List<ValidationIssue> sorted = new ArrayList<>(issues);
        sorted.sort((a, b) -> {
            int severityCompare = a.getSeverity().compareTo(b.getSeverity());
            if (severityCompare != 0) {
                return severityCompare;
            }
            return a.getSetting().compareTo(b.getSetting());
        });
        return sorted;
    }
    
    /**
     * Get only error-level issues.
     */
    public List<ValidationIssue> getErrors() {
        return issues.stream()
                .filter(ValidationIssue::isError)
                .toList();
    }
    
    /**
     * Get only warning-level issues.
     */
    public List<ValidationIssue> getWarnings() {
        return issues.stream()
                .filter(ValidationIssue::isWarning)
                .toList();
    }
    
    /**
     * Check if there are any issues.
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    /**
     * Get a summary string of the validation result.
     */
    public String getSummary() {
        if (!hasIssues()) {
            return "Configuration is valid";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Found ");
        if (errorCount > 0) {
            sb.append(errorCount).append(" error").append(errorCount > 1 ? "s" : "");
        }
        if (warningCount > 0) {
            if (errorCount > 0) sb.append(", ");
            sb.append(warningCount).append(" warning").append(warningCount > 1 ? "s" : "");
        }
        if (infoCount > 0) {
            if (errorCount > 0 || warningCount > 0) sb.append(", ");
            sb.append(infoCount).append(" info");
        }
        return sb.toString();
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public int getWarningCount() {
        return warningCount;
    }
    
    public int getInfoCount() {
        return infoCount;
    }
    
    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
    
    public void setIssues(List<ValidationIssue> issues) {
        this.issues = new ArrayList<>(issues);
        // Recalculate counts
        this.errorCount = 0;
        this.warningCount = 0;
        this.infoCount = 0;
        for (ValidationIssue issue : issues) {
            switch (issue.getSeverity()) {
                case ERROR -> errorCount++;
                case WARNING -> warningCount++;
                case INFO -> infoCount++;
            }
        }
        updateValidStatus();
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
