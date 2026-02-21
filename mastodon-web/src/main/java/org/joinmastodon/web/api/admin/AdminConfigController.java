package org.joinmastodon.web.api.admin;

import org.joinmastodon.core.dto.ValidationResult;
import org.joinmastodon.core.service.ConfigurationValidationService;
import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.auth.AdminOnly;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin API for configuration management.
 * Provides endpoints for checking configuration status and validation.
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/admin/config")
@AdminOnly(moderator = true)
public class AdminConfigController {

    private final ConfigurationValidationService validationService;

    public AdminConfigController(ConfigurationValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Get overall configuration status.
     * Returns a summary of whether the configuration is valid.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        ValidationResult result = validationService.validateAll();
        
        Map<String, Object> status = new HashMap<>();
        status.put("valid", result.isValid());
        status.put("errorCount", result.getErrorCount());
        status.put("warningCount", result.getWarningCount());
        status.put("infoCount", result.getInfoCount());
        status.put("summary", result.getSummary());
        status.put("timestamp", result.getTimestamp().toString());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get full validation result with all issues.
     */
    @GetMapping("/validate")
    public ResponseEntity<ValidationResult> validate() {
        ValidationResult result = validationService.validateAll();
        return ResponseEntity.ok(result);
    }

    /**
     * Get list of available configuration categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(validationService.getCategories());
    }

    /**
     * Validate a specific configuration category.
     */
    @GetMapping("/validate/{category}")
    public ResponseEntity<ValidationResult> validateCategory(@PathVariable String category) {
        ValidationResult result = validationService.validateCategory(category);
        return ResponseEntity.ok(result);
    }
}
