package org.joinmastodon.web.config;

import org.joinmastodon.core.dto.ValidationIssue;
import org.joinmastodon.core.dto.ValidationResult;
import org.joinmastodon.core.service.ConfigurationValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates configuration on application startup.
 * Logs errors for missing critical settings and provides actionable guidance.
 */
@Component
public class ConfigurationStartupValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationStartupValidator.class);

    private final ConfigurationValidationService validationService;

    public ConfigurationStartupValidator(ConfigurationValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Validating configuration...");
        
        ValidationResult result = validationService.validateAll();
        
        if (!result.hasIssues()) {
            log.info("Configuration validation passed - no issues found");
            return;
        }

        // Log errors
        List<ValidationIssue> errors = result.getErrors();
        if (!errors.isEmpty()) {
            log.error("=== CONFIGURATION ERRORS ===");
            log.error("Found {} critical configuration error(s) that must be fixed:", errors.size());
            for (ValidationIssue error : errors) {
                log.error("  [{}] {}", error.getSetting(), error.getMessage());
                if (error.getFixHint() != null) {
                    log.error("    Fix: {}", error.getFixHint());
                }
                if (error.getDocumentation() != null) {
                    log.error("    Docs: {}", error.getDocumentation());
                }
            }
        }

        // Log warnings
        List<ValidationIssue> warnings = result.getWarnings();
        if (!warnings.isEmpty()) {
            log.warn("=== CONFIGURATION WARNINGS ===");
            log.warn("Found {} configuration warning(s):", warnings.size());
            for (ValidationIssue warning : warnings) {
                log.warn("  [{}] {}", warning.getSetting(), warning.getMessage());
                if (warning.getFixHint() != null) {
                    log.warn("    Hint: {}", warning.getFixHint());
                }
            }
        }

        // Log info
        List<ValidationIssue> infos = result.getIssues().stream()
                .filter(i -> i.getSeverity().ordinal() == 2) // INFO
                .toList();
        if (!infos.isEmpty()) {
            log.info("=== CONFIGURATION INFO ===");
            for (ValidationIssue info : infos) {
                log.info("  [{}] {}", info.getSetting(), info.getMessage());
            }
        }

        // Summary
        log.info("Configuration validation complete: {}", result.getSummary());
        
        if (!errors.isEmpty()) {
            log.error("Application may not function correctly due to configuration errors.");
            log.error("Please fix the above issues or refer to docs/configuration.md for guidance.");
        }
    }
}
