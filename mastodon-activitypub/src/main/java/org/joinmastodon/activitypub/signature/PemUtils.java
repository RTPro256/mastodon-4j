package org.joinmastodon.activitypub.signature;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {
    private PemUtils() {
    }

    public static String toPublicKeyPem(PublicKey publicKey) {
        return toPem("PUBLIC KEY", publicKey.getEncoded());
    }

    public static String toPrivateKeyPem(PrivateKey privateKey) {
        return toPem("PRIVATE KEY", privateKey.getEncoded());
    }

    public static PublicKey parsePublicKey(String pem) {
        try {
            byte[] decoded = parsePem(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid public key PEM", ex);
        }
    }

    public static PrivateKey parsePrivateKey(String pem) {
        try {
            byte[] decoded = parsePem(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid private key PEM", ex);
        }
    }

    private static byte[] parsePem(String pem) {
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }

    private static String toPem(String label, byte[] bytes) {
        String encoded = Base64.getEncoder().encodeToString(bytes);
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN ").append(label).append("-----\n");
        for (int i = 0; i < encoded.length(); i += 64) {
            int end = Math.min(i + 64, encoded.length());
            builder.append(encoded, i, end).append("\n");
        }
        builder.append("-----END ").append(label).append("-----\n");
        return builder.toString();
    }
}
