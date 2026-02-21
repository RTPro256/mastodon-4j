package org.joinmastodon.core.service;

import org.joinmastodon.core.dto.ValidationIssue;
import org.joinmastodon.core.dto.ValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for validating server configuration.
 * Checks all configuration categories and provides actionable feedback.
 */
@Service
public class ConfigurationValidationService {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.port:25}")
    private int mailPort;

    @Value("${mastodon.federation.domain:}")
    private String federationDomain;

    @Value("${mastodon.federation.base-url:}")
    private String federationBaseUrl;

    @Value("${mastodon.federation.private-key-pem:}")
    private String privateKeyPem;

    @Value("${mastodon.federation.public-key-pem:}")
    private String publicKeyPem;

    @Value("${mastodon.media.storage-path:}")
    private String mediaStoragePath;

    @Value("${mastodon.media.base-url:}")
    private String mediaBaseUrl;

    @Value("${mastodon.setup.server.domain:}")
    private String serverDomain;

    @Value("${mastodon.setup.server.name:}")
    private String serverName;

    @Value("${mastodon.setup.server.admin-email:}")
    private String adminEmail;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.profiles.active:default}")
    private String activeProfiles;

    /**
     * Validate all configuration categories.
     */
    public ValidationResult validateAll() {
        ValidationResult result = new ValidationResult();
        
        result.merge(validateDatabase());
        result.merge(validateEmail());
        result.merge(validateFederation());
        result.merge(validateSecurity());
        result.merge(validateMedia());
        result.merge(validateServer());
        
        return result;
    }

    /**
     * Validate database configuration.
     */
    public ValidationResult validateDatabase() {
        ValidationResult result = ValidationResult.forCategory("database");
        
        // Check datasource URL
        if (datasourceUrl == null || datasourceUrl.isBlank()) {
            result.addIssue(ValidationIssue.error(
                "spring.datasource.url",
                "Database URL is not configured"
            ).withFixHint("Set SPRING_DATASOURCE_URL environment variable")
             .withDocumentation("docs/configuration.md#database"));
        } else if (datasourceUrl.contains("h2:") && isProductionProfile()) {
            result.addIssue(ValidationIssue.error(
                "spring.datasource.url",
                "H2 database should not be used in production"
            ).withFixHint("Use PostgreSQL for production: jdbc:postgresql://host/database")
             .withDocumentation("docs/configuration.md#database"));
        }

        // Check credentials
        if (datasourceUsername == null || datasourceUsername.isBlank()) {
            result.addIssue(ValidationIssue.error(
                "spring.datasource.username",
                "Database username is not configured"
            ).withFixHint("Set SPRING_DATASOURCE_USERNAME environment variable"));
        }

        if (datasourcePassword == null || datasourcePassword.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "spring.datasource.password",
                    "Database password is not configured"
                ).withFixHint("Set SPRING_DATASOURCE_PASSWORD environment variable"));
            } else {
                result.addIssue(ValidationIssue.warning(
                    "spring.datasource.password",
                    "Database password is not configured (acceptable for dev)"
                ));
            }
        }

        // Check for default credentials in production
        if (isProductionProfile() && "sa".equals(datasourceUsername)) {
            result.addIssue(ValidationIssue.error(
                "spring.datasource.username",
                "Default database credentials detected in production"
            ).withFixHint("Create a dedicated database user with limited privileges"));
        }

        return result;
    }

    /**
     * Validate email configuration.
     */
    public ValidationResult validateEmail() {
        ValidationResult result = ValidationResult.forCategory("email");
        
        if (mailHost == null || mailHost.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "spring.mail.host",
                    "Email server is not configured"
                ).withFixHint("Set SPRING_MAIL_HOST environment variable")
                 .withDocumentation("docs/configuration.md#email"));
            } else {
                result.addIssue(ValidationIssue.info(
                    "spring.mail.host",
                    "Email server not configured (emails will be logged only in dev)"
                ));
            }
        } else {
            // Validate mail configuration completeness
            if (mailUsername == null || mailUsername.isBlank()) {
                result.addIssue(ValidationIssue.warning(
                    "spring.mail.username",
                    "Email username not configured - authentication may fail"
                ));
            }

            if (mailPassword == null || mailPassword.isBlank()) {
                result.addIssue(ValidationIssue.warning(
                    "spring.mail.password",
                    "Email password not configured - authentication may fail"
                ));
            }

            // Check for common misconfigurations
            if (mailPort == 25 && isProductionProfile()) {
                result.addIssue(ValidationIssue.info(
                    "spring.mail.port",
                    "Using port 25 - consider using SMTPS (465) or submission (587) for better security"
                ));
            }
        }

        return result;
    }

    /**
     * Validate federation configuration.
     */
    public ValidationResult validateFederation() {
        ValidationResult result = ValidationResult.forCategory("federation");
        
        // Domain is critical for federation
        if (federationDomain == null || federationDomain.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "mastodon.federation.domain",
                    "Federation domain is not configured"
                ).withFixHint("Set MASTODON_FEDERATION_DOMAIN to your server's domain name")
                 .withDocumentation("docs/federation.md"));
            } else {
                result.addIssue(ValidationIssue.warning(
                    "mastodon.federation.domain",
                    "Federation domain not configured - federation will not work correctly"
                ));
            }
        } else if ("localhost".equals(federationDomain) && isProductionProfile()) {
            result.addIssue(ValidationIssue.error(
                "mastodon.federation.domain",
                "Federation domain is set to 'localhost' in production"
            ).withFixHint("Set MASTODON_FEDERATION_DOMAIN to your public domain"));
        }

        // Base URL validation
        if (federationBaseUrl == null || federationBaseUrl.isBlank()) {
            result.addIssue(ValidationIssue.warning(
                "mastodon.federation.base-url",
                "Federation base URL not configured - will use default"
            ));
        } else if (federationBaseUrl.contains("localhost") && isProductionProfile()) {
            result.addIssue(ValidationIssue.error(
                "mastodon.federation.base-url",
                "Federation base URL contains 'localhost' in production"
            ).withFixHint("Set MASTODON_FEDERATION_BASE_URL to your public URL (e.g., https://example.com)"));
        }

        // Key pair validation
        if (privateKeyPem == null || privateKeyPem.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "mastodon.federation.private-key-pem",
                    "Private key for HTTP signatures is not configured"
                ).withFixHint("Generate key pair and set MASTODON_FEDERATION_PRIVATE_KEY_PEM")
                 .withDocumentation("docs/federation.md#http-signatures"));
            } else {
                result.addIssue(ValidationIssue.info(
                    "mastodon.federation.private-key-pem",
                    "Private key not configured - will generate temporary key for development"
                ));
            }
        }

        if (publicKeyPem == null || publicKeyPem.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "mastodon.federation.public-key-pem",
                    "Public key for HTTP signatures is not configured"
                ).withFixHint("Set MASTODON_FEDERATION_PUBLIC_KEY_PEM"));
            }
        }

        return result;
    }

    /**
     * Validate security configuration.
     */
    public ValidationResult validateSecurity() {
        ValidationResult result = ValidationResult.forCategory("security");
        
        // Check profile
        if (!isProductionProfile()) {
            result.addIssue(ValidationIssue.warning(
                "spring.profiles.active",
                "Not running in production profile - security features may be relaxed"
            ).withFixHint("Set SPRING_PROFILES_ACTIVE=prod for production"));
        }

        // Check for default ports
        if (serverPort == 8080 && isProductionProfile()) {
            result.addIssue(ValidationIssue.info(
                "server.port",
                "Using default port 8080 - ensure reverse proxy is configured"
            ).withDocumentation("docs/deployment.md#reverse-proxy"));
        }

        return result;
    }

    /**
     * Validate media configuration.
     */
    public ValidationResult validateMedia() {
        ValidationResult result = ValidationResult.forCategory("media");
        
        // Storage path validation
        if (mediaStoragePath == null || mediaStoragePath.isBlank()) {
            result.addIssue(ValidationIssue.info(
                "mastodon.media.storage-path",
                "Media storage path not configured - using default 'data/media'"
            ));
        } else {
            // Check if path exists and is writable
            try {
                Path path = Path.of(mediaStoragePath);
                if (!Files.exists(path)) {
                    result.addIssue(ValidationIssue.warning(
                        "mastodon.media.storage-path",
                        "Media storage path does not exist: " + mediaStoragePath
                    ).withFixHint("Create the directory: mkdir -p " + mediaStoragePath));
                } else if (!Files.isWritable(path)) {
                    result.addIssue(ValidationIssue.error(
                        "mastodon.media.storage-path",
                        "Media storage path is not writable: " + mediaStoragePath
                    ).withFixHint("Check permissions on the directory"));
                }
            } catch (Exception e) {
                result.addIssue(ValidationIssue.error(
                    "mastodon.media.storage-path",
                    "Invalid media storage path: " + e.getMessage()
                ));
            }
        }

        // Base URL for media
        if (mediaBaseUrl == null || mediaBaseUrl.isBlank() && isProductionProfile()) {
            result.addIssue(ValidationIssue.warning(
                "mastodon.media.base-url",
                "Media base URL not configured - will use server URL"
            ).withFixHint("Set MASTODON_MEDIA_BASE_URL if using CDN or separate media server"));
        }

        return result;
    }

    /**
     * Validate server configuration.
     */
    public ValidationResult validateServer() {
        ValidationResult result = ValidationResult.forCategory("server");
        
        // Server domain
        if (serverDomain == null || serverDomain.isBlank()) {
            if (isProductionProfile()) {
                result.addIssue(ValidationIssue.error(
                    "mastodon.setup.server.domain",
                    "Server domain is not configured"
                ).withFixHint("Set MASTODON_SETUP_SERVER_DOMAIN environment variable"));
            } else {
                result.addIssue(ValidationIssue.info(
                    "mastodon.setup.server.domain",
                    "Server domain not configured - using default"
                ));
            }
        }

        // Server name
        if (serverName == null || serverName.isBlank()) {
            result.addIssue(ValidationIssue.info(
                "mastodon.setup.server.name",
                "Server name not configured - using default"
            ).withFixHint("Set MASTODON_SETUP_SERVER_NAME for your instance"));
        }

        // Admin email
        if (adminEmail == null || adminEmail.isBlank() && isProductionProfile()) {
            result.addIssue(ValidationIssue.warning(
                "mastodon.setup.server.admin-email",
                "Admin email not configured"
            ).withFixHint("Set MASTODON_SETUP_SERVER_ADMIN_EMAIL for important notifications"));
        }

        return result;
    }

    /**
     * Check if running in production profile.
     */
    private boolean isProductionProfile() {
        return activeProfiles != null && activeProfiles.contains("prod");
    }

    /**
     * Get list of all configuration categories.
     */
    public List<String> getCategories() {
        return List.of("database", "email", "federation", "security", "media", "server");
    }

    /**
     * Validate a specific category.
     */
    public ValidationResult validateCategory(String category) {
        return switch (category.toLowerCase()) {
            case "database" -> validateDatabase();
            case "email" -> validateEmail();
            case "federation" -> validateFederation();
            case "security" -> validateSecurity();
            case "media" -> validateMedia();
            case "server" -> validateServer();
            default -> {
                ValidationResult result = new ValidationResult();
                result.addIssue(ValidationIssue.error(
                    "category",
                    "Unknown configuration category: " + category
                ).withFixHint("Valid categories: " + String.join(", ", getCategories())));
                yield result;
            }
        };
    }
}
