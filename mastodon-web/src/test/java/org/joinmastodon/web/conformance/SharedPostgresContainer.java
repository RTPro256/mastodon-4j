package org.joinmastodon.web.conformance;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Singleton PostgreSQL container shared across all API conformance tests.
 * This ensures the container stays running for the entire test suite.
 */
public final class SharedPostgresContainer {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean MIGRATED = new AtomicBoolean(false);
    @SuppressWarnings("deprecation")
    private static PostgreSQLContainer<?> INSTANCE;

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            System.setProperty("ryuk.disabled", "true");
            System.setProperty("testcontainers.ryuk.disabled", "true");
            System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
            String dockerHostEnv = System.getenv("DOCKER_HOST");
            if (dockerHostEnv == null || dockerHostEnv.contains("docker_cli")) {
                String dockerHost = "npipe:////./pipe/docker_engine";
                System.setProperty("DOCKER_HOST", dockerHost);
                System.setProperty("docker.host", dockerHost);
                System.setProperty("docker.client.strategy",
                        "org.testcontainers.dockerclient.NpipeSocketClientProviderStrategy");
            }
        }
    }

    private SharedPostgresContainer() {
        // Private constructor to prevent instantiation
    }

    @SuppressWarnings({"deprecation", "resource"})
    public static PostgreSQLContainer<?> getInstance() {
        if (INSTANCE == null) {
            synchronized (SharedPostgresContainer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
                            .withDatabaseName("mastodon_test")
                            .withUsername("mastodon")
                            .withPassword("mastodon")
                            .withReuse(true);  // Enable container reuse
                }
            }
        }
        return INSTANCE;
    }

    @SuppressWarnings("deprecation")
    public static void startAndMigrate() {
        PostgreSQLContainer<?> container = getInstance();
        if (STARTED.compareAndSet(false, true)) {
            container.start();
        }
        if (MIGRATED.compareAndSet(false, true)) {
            Flyway.configure()
                    .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        }
    }

    public static String getJdbcUrl() {
        return getInstance().getJdbcUrl();
    }

    public static String getUsername() {
        return getInstance().getUsername();
    }

    public static String getPassword() {
        return getInstance().getPassword();
    }

    public static String getDriverClassName() {
        return getInstance().getDriverClassName();
    }
}
