package org.joinmastodon.activitypub.signature;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HttpSignature {
    private String keyId;
    private String algorithm;
    private List<String> headers;
    private String signature;

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public static HttpSignature parse(String headerValue) {
        HttpSignature signature = new HttpSignature();
        Map<String, String> parts = HeaderParser.parseHeader(headerValue);
        signature.setKeyId(parts.get("keyId"));
        signature.setAlgorithm(parts.getOrDefault("algorithm", "rsa-sha256"));
        String headers = parts.getOrDefault("headers", "(request-target) host date");
        signature.setHeaders(splitHeaders(headers));
        signature.setSignature(parts.get("signature"));
        return signature;
    }

    private static List<String> splitHeaders(String headerList) {
        String[] parts = headerList.trim().split("\\s+");
        List<String> headers = new ArrayList<>();
        for (String part : parts) {
            headers.add(part.toLowerCase(Locale.ROOT));
        }
        return headers;
    }
}
