package org.joinmastodon.core.dto;

/**
 * Represents a single validation issue for a configuration setting.
 */
public class ValidationIssue {
    
    private String setting;
    private IssueSeverity severity;
    private String message;
    private String documentation;
    private String fixCommand;
    private String fixHint;
    
    public ValidationIssue() {
    }
    
    public ValidationIssue(String setting, IssueSeverity severity, String message) {
        this.setting = setting;
        this.severity = severity;
        this.message = message;
    }
    
    /**
     * Create an error-level issue.
     */
    public static ValidationIssue error(String setting, String message) {
        return new ValidationIssue(setting, IssueSeverity.ERROR, message);
    }
    
    /**
     * Create a warning-level issue.
     */
    public static ValidationIssue warning(String setting, String message) {
        return new ValidationIssue(setting, IssueSeverity.WARNING, message);
    }
    
    /**
     * Create an info-level issue.
     */
    public static ValidationIssue info(String setting, String message) {
        return new ValidationIssue(setting, IssueSeverity.INFO, message);
    }
    
    /**
     * Set the documentation link for this issue.
     */
    public ValidationIssue withDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }
    
    /**
     * Set the fix command for this issue.
     */
    public ValidationIssue withFixCommand(String fixCommand) {
        this.fixCommand = fixCommand;
        return this;
    }
    
    /**
     * Set the fix hint for this issue.
     */
    public ValidationIssue withFixHint(String fixHint) {
        this.fixHint = fixHint;
        return this;
    }
    
    public String getSetting() {
        return setting;
    }
    
    public void setSetting(String setting) {
        this.setting = setting;
    }
    
    public IssueSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDocumentation() {
        return documentation;
    }
    
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
    
    public String getFixCommand() {
        return fixCommand;
    }
    
    public void setFixCommand(String fixCommand) {
        this.fixCommand = fixCommand;
    }
    
    public String getFixHint() {
        return fixHint;
    }
    
    public void setFixHint(String fixHint) {
        this.fixHint = fixHint;
    }
    
    public boolean isError() {
        return severity == IssueSeverity.ERROR;
    }
    
    public boolean isWarning() {
        return severity == IssueSeverity.WARNING;
    }
}
