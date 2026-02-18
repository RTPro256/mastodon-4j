package org.joinmastodon.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test controller to verify the application is working.
 * This provides basic endpoints for testing connectivity.
 */
@RestController
@RequestMapping("/api/v1")
public class TestController {
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Mastodon Java is running!");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Mastodon Java!";
    }
}
