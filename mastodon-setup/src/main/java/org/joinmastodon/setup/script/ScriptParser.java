package org.joinmastodon.setup.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.joinmastodon.setup.config.SetupConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for YAML/JSON setup scripts.
 * Supports both YAML and JSON formats for configuration files.
 */
@Component
public class ScriptParser {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public ScriptParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.findAndRegisterModules();
        
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.findAndRegisterModules();
    }

    /**
     * Parse a setup script from a file.
     * Automatically detects YAML or JSON based on file extension.
     *
     * @param file the setup script file
     * @return parsed configuration
     * @throws IOException if parsing fails
     */
    public SetupConfiguration parse(File file) throws IOException {
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
            return yamlMapper.readValue(file, SetupConfiguration.class);
        } else if (filename.endsWith(".json")) {
            return jsonMapper.readValue(file, SetupConfiguration.class);
        }
        throw new IllegalArgumentException("Unsupported file format: " + filename + 
            ". Supported formats: .yaml, .yml, .json");
    }

    /**
     * Parse a setup script from an input stream.
     *
     * @param inputStream the input stream containing YAML or JSON
     * @param isYaml true if YAML format, false if JSON
     * @return parsed configuration
     * @throws IOException if parsing fails
     */
    public SetupConfiguration parse(InputStream inputStream, boolean isYaml) throws IOException {
        ObjectMapper mapper = isYaml ? yamlMapper : jsonMapper;
        return mapper.readValue(inputStream, SetupConfiguration.class);
    }

    /**
     * Parse a setup script from a string.
     *
     * @param content the YAML or JSON content
     * @param isYaml true if YAML format, false if JSON
     * @return parsed configuration
     * @throws IOException if parsing fails
     */
    public SetupConfiguration parse(String content, boolean isYaml) throws IOException {
        ObjectMapper mapper = isYaml ? yamlMapper : jsonMapper;
        return mapper.readValue(content, SetupConfiguration.class);
    }

    /**
     * Parse a setup script from a string, auto-detecting format.
     * Defaults to YAML if format cannot be determined.
     *
     * @param content the YAML or JSON content
     * @return parsed configuration
     * @throws IOException if parsing fails
     */
    public SetupConfiguration parse(String content) throws IOException {
        // Auto-detect: JSON starts with { or [, YAML typically doesn't
        boolean isJson = content.trim().startsWith("{") || content.trim().startsWith("[");
        return parse(content, !isJson);
    }

    /**
     * Export a configuration to YAML format.
     *
     * @param config the configuration to export
     * @return YAML string representation
     * @throws IOException if serialization fails
     */
    public String export(SetupConfiguration config) throws IOException {
        return yamlMapper.writeValueAsString(config);
    }

    /**
     * Export a configuration to JSON format.
     *
     * @param config the configuration to export
     * @return JSON string representation
     * @throws IOException if serialization fails
     */
    public String exportJson(SetupConfiguration config) throws IOException {
        return jsonMapper.writeValueAsString(config);
    }

    /**
     * Validate a setup configuration.
     *
     * @param config the configuration to validate
     * @return validation result
     */
    public ValidationResult validate(SetupConfiguration config) {
        ValidationResult result = new ValidationResult();
        
        // Validate server config
        if (config.getServer().getDomain() == null || config.getServer().getDomain().isBlank()) {
            result.addError("server.domain", "Domain is required");
        }
        if (config.getServer().getName() == null || config.getServer().getName().isBlank()) {
            result.addError("server.name", "Server name is required");
        }
        
        // Validate resource config
        if (config.getResources().getCpuCores() < 1) {
            result.addError("resources.cpuCores", "CPU cores must be at least 1");
        }
        if (config.getResources().getMemoryGb() < 1) {
            result.addError("resources.memoryGb", "Memory must be at least 1 GB");
        }
        
        return result;
    }

    /**
     * Validation result container.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private final java.util.List<ValidationError> errors = new java.util.ArrayList<>();

        public void addError(String field, String message) {
            errors.add(new ValidationError(field, message));
            valid = false;
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.List<ValidationError> getErrors() {
            return errors;
        }
    }

    /**
     * Validation error details.
     */
    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
