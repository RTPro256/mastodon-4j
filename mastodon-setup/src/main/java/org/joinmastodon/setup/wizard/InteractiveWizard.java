package org.joinmastodon.setup.wizard;

import org.joinmastodon.setup.config.SetupConfiguration;
import org.joinmastodon.setup.config.SetupConfiguration.*;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Interactive CLI wizard for guided server setup.
 * Provides step-by-step configuration with sensible defaults.
 */
@Component
public class InteractiveWizard {

    private final Scanner scanner;

    public InteractiveWizard() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Run the interactive setup wizard.
     *
     * @return completed configuration
     */
    public SetupConfiguration run() {
        System.out.println("\n========================================");
        System.out.println("  Mastodon Server Setup Wizard");
        System.out.println("========================================\n");

        SetupConfiguration config = new SetupConfiguration();

        // Step 1: Server Configuration
        System.out.println("Step 1: Server Configuration");
        System.out.println("----------------------------------------");
        configureServer(config);

        // Step 2: Resource Allocation
        System.out.println("\nStep 2: Resource Allocation");
        System.out.println("----------------------------------------");
        configureResources(config);

        // Step 3: Content Policy
        System.out.println("\nStep 3: Content Policy");
        System.out.println("----------------------------------------");
        configureContent(config);

        // Step 4: Federation Index
        System.out.println("\nStep 4: Federation Index");
        System.out.println("----------------------------------------");
        configureFederationIndex(config);

        // Summary
        System.out.println("\n========================================");
        System.out.println("  Configuration Summary");
        System.out.println("========================================");
        printSummary(config);

        // Confirm
        if (confirmConfiguration()) {
            System.out.println("\nConfiguration complete! Applying settings...");
            return config;
        } else {
            System.out.println("\nRestarting setup wizard...");
            return run(); // Restart
        }
    }

    private void configureServer(SetupConfiguration config) {
        ServerConfig server = config.getServer();

        System.out.print("Server domain (e.g., social.example.com): ");
        server.setDomain(scanner.nextLine().trim());

        System.out.print("Server name (e.g., Example Social): ");
        server.setName(scanner.nextLine().trim());

        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) {
            server.setDescription(description);
        }

        System.out.print("Admin email (optional): ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            server.setAdminEmail(email);
        }

        System.out.print("Allow open registrations? (Y/n): ");
        String registrations = scanner.nextLine().trim().toLowerCase();
        server.setRegistrationsOpen(!registrations.equals("n"));

        if (server.isRegistrationsOpen()) {
            System.out.print("Require approval for new accounts? (y/N): ");
            String approval = scanner.nextLine().trim().toLowerCase();
            server.setApprovalRequired(approval.equals("y"));
        }
    }

    private void configureResources(SetupConfiguration config) {
        ResourceConfig resources = config.getResources();
        int availableCores = Runtime.getRuntime().availableProcessors();

        System.out.println("Available CPU cores: " + availableCores);
        System.out.print("CPU cores to use (default: " + availableCores + "): ");
        String cores = scanner.nextLine().trim();
        if (!cores.isEmpty()) {
            resources.setCpuCores(Integer.parseInt(cores));
        } else {
            resources.setCpuCores(availableCores);
        }

        System.out.print("Memory limit in GB (default: 4): ");
        String memory = scanner.nextLine().trim();
        if (!memory.isEmpty()) {
            resources.setMemoryGb(Integer.parseInt(memory));
        }

        System.out.print("Enable GPU acceleration? (y/N): ");
        String gpu = scanner.nextLine().trim().toLowerCase();
        resources.setGpuEnabled(gpu.equals("y"));
    }

    private void configureContent(SetupConfiguration config) {
        ContentConfig content = config.getContent();

        System.out.println("Default visibility options:");
        System.out.println("  1. public - Visible to everyone");
        System.out.println("  2. unlisted - Visible but not in public timelines");
        System.out.println("  3. private - Followers only");
        System.out.print("Default visibility (1-3, default: 1): ");
        String visibility = scanner.nextLine().trim();
        switch (visibility) {
            case "2" -> content.setDefaultVisibility("unlisted");
            case "3" -> content.setDefaultVisibility("private");
            default -> content.setDefaultVisibility("public");
        }

        System.out.print("Enable private content features? (y/N): ");
        String privateContent = scanner.nextLine().trim().toLowerCase();
        content.setPrivateContentEnabled(privateContent.equals("y"));

        System.out.println("Federation mode options:");
        System.out.println("  1. open - Federate with all instances");
        System.out.println("  2. limited - Federate with approved instances only");
        System.out.println("  3. closed - No federation");
        System.out.print("Federation mode (1-3, default: 1): ");
        String federation = scanner.nextLine().trim();
        switch (federation) {
            case "2" -> content.setFederationMode(FederationMode.LIMITED);
            case "3" -> content.setFederationMode(FederationMode.CLOSED);
            default -> content.setFederationMode(FederationMode.OPEN);
        }
    }

    private void configureFederationIndex(SetupConfiguration config) {
        FederationIndexConfig index = config.getFederationIndex();

        System.out.print("Enable federation index? (Y/n): ");
        String enabled = scanner.nextLine().trim().toLowerCase();
        index.setEnabled(!enabled.equals("n"));

        if (index.isEnabled()) {
            System.out.print("Share server ratings with other instances? (Y/n): ");
            String shareRatings = scanner.nextLine().trim().toLowerCase();
            index.setShareRatings(!shareRatings.equals("n"));

            System.out.print("Share restriction lists? (y/N): ");
            String shareRestrictions = scanner.nextLine().trim().toLowerCase();
            index.setShareRestrictions(shareRestrictions.equals("y"));
        }
    }

    private void printSummary(SetupConfiguration config) {
        ServerConfig server = config.getServer();
        ResourceConfig resources = config.getResources();
        ContentConfig content = config.getContent();
        FederationIndexConfig index = config.getFederationIndex();

        System.out.println("\nServer:");
        System.out.println("  Domain: " + server.getDomain());
        System.out.println("  Name: " + server.getName());
        System.out.println("  Open registrations: " + (server.isRegistrationsOpen() ? "Yes" : "No"));
        if (server.isRegistrationsOpen() && server.isApprovalRequired()) {
            System.out.println("  Approval required: Yes");
        }

        System.out.println("\nResources:");
        System.out.println("  CPU cores: " + resources.getCpuCores());
        System.out.println("  Memory: " + resources.getMemoryGb() + " GB");
        System.out.println("  GPU acceleration: " + (resources.isGpuEnabled() ? "Enabled" : "Disabled"));

        System.out.println("\nContent:");
        System.out.println("  Default visibility: " + content.getDefaultVisibility());
        System.out.println("  Federation mode: " + content.getFederationMode());

        System.out.println("\nFederation Index:");
        System.out.println("  Enabled: " + (index.isEnabled() ? "Yes" : "No"));
        if (index.isEnabled()) {
            System.out.println("  Share ratings: " + (index.isShareRatings() ? "Yes" : "No"));
            System.out.println("  Share restrictions: " + (index.isShareRestrictions() ? "Yes" : "No"));
        }
    }

    private boolean confirmConfiguration() {
        System.out.print("\nApply this configuration? (Y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        return !confirm.equals("n");
    }
}
