package org.joinmastodon.torrent.readme;

import org.joinmastodon.torrent.authority.ContentAuthorityRecord;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Generates README.md content for torrent files.
 * 
 * The README contains:
 * - Mastodon server name
 * - Content creator information
 * - Content authority verification guide
 */
public class TorrentReadmeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.systemDefault());

    private final String serverName;
    private final String serverUrl;
    private final String contentAuthorityEndpoint;

    /**
     * Create a new TorrentReadmeGenerator.
     *
     * @param serverName the name of the Mastodon server
     * @param serverUrl the base URL of the Mastodon server
     * @param contentAuthorityEndpoint the endpoint for content authority verification
     */
    public TorrentReadmeGenerator(String serverName, String serverUrl, String contentAuthorityEndpoint) {
        this.serverName = serverName;
        this.serverUrl = serverUrl;
        this.contentAuthorityEndpoint = contentAuthorityEndpoint;
    }

    /**
     * Generate README.md content for a torrent.
     *
     * @param request the readme generation request
     * @return the README.md content as bytes
     */
    public byte[] generateReadme(ReadmeRequest request) {
        return generateReadme(request, null);
    }

    /**
     * Generate README.md content for a torrent with content authority.
     *
     * @param request the readme generation request
     * @param authorityRecord the content authority record (can be null)
     * @return the README.md content as bytes
     */
    public byte[] generateReadme(ReadmeRequest request, ContentAuthorityRecord authorityRecord) {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("# ").append(escapeMarkdown(request.getContentTitle())).append("\n\n");
        
        // Server information
        sb.append("## Source Server\n\n");
        sb.append("- **Server**: ").append(escapeMarkdown(serverName)).append("\n");
        sb.append("- **Server URL**: ").append(serverUrl).append("\n\n");
        
        // Creator information
        sb.append("## Content Creator\n\n");
        sb.append("- **Username**: @").append(escapeMarkdown(request.getCreatorUsername()));
        if (request.getCreatorDisplayName() != null) {
            sb.append(" (").append(escapeMarkdown(request.getCreatorDisplayName())).append(")");
        }
        sb.append("\n");
        sb.append("- **Account URL**: ").append(serverUrl).append("/@")
                .append(encodeUrl(request.getCreatorUsername())).append("\n");
        if (request.getCreatorAccountId() != null) {
            sb.append("- **Account ID**: ").append(request.getCreatorAccountId()).append("\n");
        }
        sb.append("\n");
        
        // Content information
        sb.append("## Content Information\n\n");
        sb.append("- **Content Type**: ").append(request.getContentType()).append("\n");
        sb.append("- **Created**: ").append(DATE_FORMATTER.format(Instant.ofEpochMilli(request.getCreatedAt()))).append("\n");
        if (request.getContentDescription() != null && !request.getContentDescription().isBlank()) {
            sb.append("- **Description**: ").append(escapeMarkdown(request.getContentDescription())).append("\n");
        }
        sb.append("\n");
        
        // Torrent information
        sb.append("## Torrent Information\n\n");
        sb.append("- **Infohash**: `").append(request.getInfohash()).append("`\n");
        if (request.getTotalSize() != null) {
            sb.append("- **Total Size**: ").append(formatSize(request.getTotalSize())).append("\n");
        }
        sb.append("\n");
        
        // Content Authority Verification Guide
        sb.append("## Content Authority Verification\n\n");
        
        if (authorityRecord != null) {
            sb.append("This torrent was created by a verified user on **").append(escapeMarkdown(serverName)).append("**.\n");
            sb.append("The content is cryptographically signed to prove its authenticity.\n\n");
            
            sb.append("### Authority Record\n\n");
            sb.append("- **Signature Algorithm**: ").append(authorityRecord.getSignatureAlgorithm()).append("\n");
            sb.append("- **Verification URL**: ").append(authorityRecord.getVerificationUrl()).append("\n\n");
        } else {
            sb.append("This torrent was created by a user on **").append(escapeMarkdown(serverName)).append("**.\n");
            sb.append("You can verify the authenticity of this content using our Content Authority service.\n\n");
        }
        
        sb.append("### How to Verify\n\n");
        sb.append("1. **Using the API**:\n");
        sb.append("   ```\n");
        sb.append("   GET ").append(serverUrl).append(contentAuthorityEndpoint);
        sb.append("/").append(request.getInfohash()).append("\n");
        sb.append("   ```\n\n");
        
        sb.append("2. **Expected Response**:\n");
        sb.append("   ```json\n");
        sb.append("   {\n");
        sb.append("     \"infohash\": \"").append(request.getInfohash()).append("\",\n");
        sb.append("     \"creator_account_id\": \"").append(request.getCreatorAccountId() != null ? request.getCreatorAccountId().toString() : "").append("\",\n");
        sb.append("     \"creator_username\": \"").append(escapeJson(request.getCreatorUsername())).append("\",\n");
        sb.append("     \"created_at\": \"").append(Instant.ofEpochMilli(request.getCreatedAt())).append("\",\n");
        if (authorityRecord != null) {
            sb.append("     \"server_name\": \"").append(escapeJson(authorityRecord.getServerName())).append("\",\n");
            sb.append("     \"signature\": \"").append(authorityRecord.getSignature()).append("\",\n");
            sb.append("     \"signature_algorithm\": \"").append(authorityRecord.getSignatureAlgorithm()).append("\",\n");
            sb.append("     \"server_public_key\": \"").append(authorityRecord.getServerPublicKey()).append("\"\n");
        } else {
            sb.append("     \"signature\": \"<cryptographic_signature>\"\n");
        }
        sb.append("   }\n");
        sb.append("   ```\n\n");
        
        sb.append("3. **Verification Steps**:\n");
        sb.append("   - Download the content via torrent\n");
        sb.append("   - Fetch the authority record from the verification URL\n");
        sb.append("   - Verify the server's RSA signature using the provided public key\n");
        sb.append("   - Confirm the creator account matches the expected user\n\n");
        
        sb.append("4. **Programmatic Verification** (Java example):\n");
        sb.append("   ```java\n");
        sb.append("   // Fetch the authority record\n");
        sb.append("   ContentAuthorityRecord record = fetchAuthorityRecord(infohash);\n");
        sb.append("   \n");
        sb.append("   // Verify the signature\n");
        sb.append("   ContentAuthorityService service = new ContentAuthorityService(\n");
        sb.append("       record.getServerName(), record.getServerUrl()\n");
        sb.append("   );\n");
        sb.append("   boolean valid = service.verifyContent(record);\n");
        sb.append("   ```\n\n");
        
        // License and usage
        sb.append("## License and Usage\n\n");
        if (request.getLicense() != null && !request.getLicense().isBlank()) {
            sb.append("This content is licensed under: **").append(escapeMarkdown(request.getLicense())).append("**\n\n");
        } else {
            sb.append("Please respect the creator's rights and follow your local laws when using this content.\n\n");
        }
        
        // Disclaimer
        sb.append("---\n\n");
        sb.append("*This README was automatically generated by ").append(escapeMarkdown(serverName));
        sb.append(" on ").append(DATE_FORMATTER.format(Instant.now())).append(".*\n");
        sb.append("*Torrent technology is used for decentralized content distribution. ");
        sb.append("The server is not responsible for content availability after distribution.*\n");
        
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get the filename for the README file.
     *
     * @return the README filename
     */
    public String getReadmeFilename() {
        return "README.md";
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("#", "\\#")
                .replace("`", "\\`");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String encodeUrl(String text) {
        return text.replace(" ", "%20");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
