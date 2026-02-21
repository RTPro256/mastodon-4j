package org.joinmastodon.web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database schema validation test to ensure all entity fields have corresponding database columns.
 * This test validates that the database schema supports all features implemented in the codebase.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Database Schema Validation Tests")
public class DatabaseSchemaValidationTest {

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    )
            .withDatabaseName("mastodon_test")
            .withUsername("test")
            .withPassword("test");

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
            ds.setJdbcUrl(postgres.getJdbcUrl());
            ds.setUsername(postgres.getUsername());
            ds.setPassword(postgres.getPassword());
            ds.setDriverClassName("org.postgresql.Driver");
            return ds;
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Users table should have all required columns")
    void usersTableShouldHaveAllRequiredColumns() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'users'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        // Core columns
        assertTrue(columnNames.contains("id"), "users should have 'id' column");
        assertTrue(columnNames.contains("account_id"), "users should have 'account_id' column");
        assertTrue(columnNames.contains("email"), "users should have 'email' column");
        assertTrue(columnNames.contains("password_hash"), "users should have 'password_hash' column");
        assertTrue(columnNames.contains("locale"), "users should have 'locale' column");
        assertTrue(columnNames.contains("created_at"), "users should have 'created_at' column");
        assertTrue(columnNames.contains("last_sign_in_at"), "users should have 'last_sign_in_at' column");
        
        // New feature columns
        assertTrue(columnNames.contains("last_sign_in_ip"), "users should have 'last_sign_in_ip' column for IP tracking");
        assertTrue(columnNames.contains("confirmed_at"), "users should have 'confirmed_at' column for email confirmation");
        assertTrue(columnNames.contains("confirmed"), "users should have 'confirmed' column for email confirmation");
        assertTrue(columnNames.contains("approved"), "users should have 'approved' column for approval workflow");
        assertTrue(columnNames.contains("approval_required"), "users should have 'approval_required' column for approval workflow");
        assertTrue(columnNames.contains("role"), "users should have 'role' column for RBAC");
    }

    @Test
    @DisplayName("Follows table should have pending column for follow requests")
    void followsTableShouldHavePendingColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'follows'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("id"), "follows should have 'id' column");
        assertTrue(columnNames.contains("account_id"), "follows should have 'account_id' column");
        assertTrue(columnNames.contains("target_account_id"), "follows should have 'target_account_id' column");
        assertTrue(columnNames.contains("created_at"), "follows should have 'created_at' column");
        assertTrue(columnNames.contains("pending"), "follows should have 'pending' column for follow requests");
    }

    @Test
    @DisplayName("User domain blocks table should exist")
    void userDomainBlocksTableShouldExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'user_domain_blocks'",
                Integer.class
        );
        
        assertEquals(1, count, "user_domain_blocks table should exist");
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'user_domain_blocks'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("id"), "user_domain_blocks should have 'id' column");
        assertTrue(columnNames.contains("account_id"), "user_domain_blocks should have 'account_id' column");
        assertTrue(columnNames.contains("domain"), "user_domain_blocks should have 'domain' column");
        assertTrue(columnNames.contains("created_at"), "user_domain_blocks should have 'created_at' column");
    }

    @Test
    @DisplayName("Status pins table should exist")
    void statusPinsTableShouldExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'status_pins'",
                Integer.class
        );
        
        assertEquals(1, count, "status_pins table should exist");
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'status_pins'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("id"), "status_pins should have 'id' column");
        assertTrue(columnNames.contains("account_id"), "status_pins should have 'account_id' column");
        assertTrue(columnNames.contains("status_id"), "status_pins should have 'status_id' column");
        assertTrue(columnNames.contains("created_at"), "status_pins should have 'created_at' column");
    }

    @Test
    @DisplayName("Accounts table should have moderation columns")
    void accountsTableShouldHaveModerationColumns() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'accounts'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        // Moderation columns
        assertTrue(columnNames.contains("suspended"), "accounts should have 'suspended' column");
        assertTrue(columnNames.contains("silenced"), "accounts should have 'silenced' column");
        assertTrue(columnNames.contains("disabled"), "accounts should have 'disabled' column");
        
        // Federation columns
        assertTrue(columnNames.contains("inbox_url"), "accounts should have 'inbox_url' column for federation");
        assertTrue(columnNames.contains("public_key_pem"), "accounts should have 'public_key_pem' column for federation");
    }

    @Test
    @DisplayName("Reports table should have admin columns")
    void reportsTableShouldHaveAdminColumns() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'reports'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("assigned_account_id"), "reports should have 'assigned_account_id' column");
        assertTrue(columnNames.contains("action_taken_at"), "reports should have 'action_taken_at' column");
        assertTrue(columnNames.contains("action_taken_by_account_id"), "reports should have 'action_taken_by_account_id' column");
        assertTrue(columnNames.contains("forwarded"), "reports should have 'forwarded' column");
    }

    @Test
    @DisplayName("Domain blocks table should exist for instance-level moderation")
    void domainBlocksTableShouldExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'domain_blocks'",
                Integer.class
        );
        
        assertEquals(1, count, "domain_blocks table should exist");
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'domain_blocks'"
        );
        
        List<String> columnNames = columns.stream()
                .map(row -> (String) row.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("id"), "domain_blocks should have 'id' column");
        assertTrue(columnNames.contains("domain"), "domain_blocks should have 'domain' column");
        assertTrue(columnNames.contains("severity"), "domain_blocks should have 'severity' column");
        assertTrue(columnNames.contains("reject_media"), "domain_blocks should have 'reject_media' column");
        assertTrue(columnNames.contains("reject_reports"), "domain_blocks should have 'reject_reports' column");
    }

    @Test
    @DisplayName("Account actions table should exist for moderation tracking")
    void accountActionsTableShouldExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'account_actions'",
                Integer.class
        );
        
        assertEquals(1, count, "account_actions table should exist");
    }

    @Test
    @DisplayName("Federation tables should exist")
    void federationTablesShouldExist() {
        // Federation deliveries table
        Integer deliveriesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'federation_deliveries'",
                Integer.class
        );
        assertEquals(1, deliveriesCount, "federation_deliveries table should exist");
        
        // Follow requests table (for remote follow requests)
        Integer followRequestsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'follow_requests'",
                Integer.class
        );
        assertEquals(1, followRequestsCount, "follow_requests table should exist");
    }

    @Test
    @DisplayName("Search indexes should be created")
    void searchIndexesShouldBeCreated() {
        // Check for pg_trgm extension
        Integer trgmCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_extension WHERE extname = 'pg_trgm'",
                Integer.class
        );
        assertEquals(1, trgmCount, "pg_trgm extension should be installed for search");
        
        // Check for tsvector indexes on statuses
        List<Map<String, Object>> statusIndexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'statuses' AND indexname LIKE '%search%'"
        );
        assertFalse(statusIndexes.isEmpty(), "statuses table should have search indexes");
    }

    @Test
    @DisplayName("All required tables should exist")
    void allRequiredTablesShouldExist() {
        String[] requiredTables = {
            "accounts", "users", "statuses", "follows", "favourites", "bookmarks",
            "blocks", "mutes", "notifications", "media_attachments", "mentions",
            "tags", "polls", "poll_options", "poll_votes", "lists", "list_accounts",
            "filters", "filter_keywords", "reports", "applications",
            "oauth_access_tokens", "oauth_refresh_tokens", "oauth_authorization_codes",
            "sessions", "jobs", "user_domain_blocks", "status_pins",
            "account_actions", "domain_blocks", "report_notes", "federation_deliveries"
        };
        
        for (String table : requiredTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                    Integer.class,
                    table
            );
            assertEquals(1, count, "Table '" + table + "' should exist");
        }
    }
}