package org.joinmastodon.core.dto;

/**
 * Severity level for a validation issue.
 */
public enum IssueSeverity {
    /**
     * Critical error - application may not function correctly.
     */
    ERROR,
    
    /**
     * Warning - functionality may be limited.
     */
    WARNING,
    
    /**
     * Informational - recommended but not required.
     */
    INFO
}
