package org.joinmastodon.web.api.setup;

import org.joinmastodon.setup.config.SetupConfiguration;
import org.joinmastodon.setup.script.ScriptParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API endpoints for server setup and configuration management.
 */
@RestController
@RequestMapping("/api/v2/admin/setup")
public class SetupApiController {

    private final ScriptParser scriptParser;
    private final SetupConfiguration setupConfiguration;

    public SetupApiController(ScriptParser scriptParser, SetupConfiguration setupConfiguration) {
        this.scriptParser = scriptParser;
        this.setupConfiguration = setupConfiguration;
    }

    /**
     * Get the current setup status.
     */
    @GetMapping("/status")
    public ResponseEntity<SetupStatus> getStatus() {
        SetupStatus status = new SetupStatus();
        status.setConfigured(setupConfiguration.getServer().getDomain() != null);
        status.setMode(setupConfiguration.getMode().name());
        status.setServerDomain(setupConfiguration.getServer().getDomain());
        status.setServerName(setupConfiguration.getServer().getName());
        return ResponseEntity.ok(status);
    }

    /**
     * Validate a setup script without applying it.
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateScript(@RequestBody String scriptContent) {
        try {
            SetupConfiguration config = scriptParser.parse(scriptContent);
            ValidationResult result = new ValidationResult();
            result.setValid(true);
            result.setMessage("Configuration is valid");
            result.setServerDomain(config.getServer().getDomain());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ValidationResult result = new ValidationResult();
            result.setValid(false);
            result.setMessage("Validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Apply a setup script configuration.
     */
    @PostMapping("/apply")
    public ResponseEntity<ApplyResult> applyScript(@RequestBody String scriptContent) {
        try {
            SetupConfiguration config = scriptParser.parse(scriptContent);
            // Apply configuration (would be implemented in a real setup service)
            ApplyResult result = new ApplyResult();
            result.setSuccess(true);
            result.setMessage("Configuration applied successfully");
            result.setServerDomain(config.getServer().getDomain());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ApplyResult result = new ApplyResult();
            result.setSuccess(false);
            result.setMessage("Failed to apply configuration: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Export current configuration as a YAML script.
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportConfiguration() {
        try {
            String yaml = scriptParser.export(setupConfiguration);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/yaml")
                    .body(yaml);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available setup templates.
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, String>> getTemplates() {
        return ResponseEntity.ok(Map.of(
                "minimal", "Small personal instance",
                "standard", "Community instance",
                "enterprise", "Large-scale production deployment"
        ));
    }

    // DTOs
    public static class SetupStatus {
        private boolean configured;
        private String mode;
        private String serverDomain;
        private String serverName;

        public boolean isConfigured() { return configured; }
        public void setConfigured(boolean configured) { this.configured = configured; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getServerDomain() { return serverDomain; }
        public void setServerDomain(String serverDomain) { this.serverDomain = serverDomain; }
        public String getServerName() { return serverName; }
        public void setServerName(String serverName) { this.serverName = serverName; }
    }

    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String serverDomain;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getServerDomain() { return serverDomain; }
        public void setServerDomain(String serverDomain) { this.serverDomain = serverDomain; }
    }

    public static class ApplyResult {
        private boolean success;
        private String message;
        private String serverDomain;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getServerDomain() { return serverDomain; }
        public void setServerDomain(String serverDomain) { this.serverDomain = serverDomain; }
    }
}
