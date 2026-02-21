package org.joinmastodon.activitypub.signature;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class HttpSignatureSigner {
    public String sign(String keyId, PrivateKey privateKey, String method, String path,
                       Map<String, String> headers, List<String> signedHeaders) {
        try {
            String signingString = buildSigningString(method, path, headers, signedHeaders);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signingString.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(signature.sign());
            StringJoiner joiner = new StringJoiner(" ");
            for (String header : signedHeaders) {
                joiner.add(header);
            }
            return "keyId=\"" + keyId + "\",algorithm=\"rsa-sha256\",headers=\"" + joiner +
                    "\",signature=\"" + encoded + "\"";
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign request", ex);
        }
    }

    public String buildSigningString(String method, String path, Map<String, String> headers,
                                     List<String> signedHeaders) {
        StringJoiner joiner = new StringJoiner("\n");
        for (String header : signedHeaders) {
            String lower = header.toLowerCase(Locale.ROOT);
            if ("(request-target)".equals(lower)) {
                joiner.add("(request-target): " + method.toLowerCase(Locale.ROOT) + " " + path);
                continue;
            }
            // Case-insensitive header lookup
            String value = headers.get(lower);
            if (value == null) {
                value = headers.get(header);
            }
            if (value == null) {
                // Try to find header with case-insensitive matching
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getKey().toLowerCase(Locale.ROOT).equals(lower)) {
                        value = entry.getValue();
                        break;
                    }
                }
            }
            if (value == null) {
                throw new IllegalArgumentException("Missing header for signature: " + header);
            }
            joiner.add(lower + ": " + value);
        }
        return joiner.toString();
    }
}
