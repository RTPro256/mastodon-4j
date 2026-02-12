package org.joinmastodon.activitypub.signature;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HttpSignatureVerifier {
    public boolean verify(HttpSignature signature, PublicKey publicKey, String method, String path,
                          Map<String, String> headers) {
        if (signature == null || publicKey == null) {
            return false;
        }
        if (signature.getSignature() == null) {
            return false;
        }
        List<String> signedHeaders = signature.getHeaders();
        try {
            HttpSignatureSigner signer = new HttpSignatureSigner();
            String signingString = signer.buildSigningString(method, path, headers, signedHeaders);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(signingString.getBytes(StandardCharsets.UTF_8));
            byte[] decoded = Base64.getDecoder().decode(signature.getSignature());
            return verifier.verify(decoded);
        } catch (Exception ex) {
            return false;
        }
    }
}
