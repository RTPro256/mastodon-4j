package org.joinmastodon.web.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1)
public class InstanceController {

    @GetMapping("/instance")
    public Map<String, Object> instance() {
        return Map.of(
                "uri", "localhost",
                "title", "Mastodon Java",
                "description", "A Mastodon instance running on Java",
                "version", "4.5.6",
                "stats", Map.of(
                        "user_count", 0,
                        "status_count", 0,
                        "domain_count", 0
                )
        );
    }
}
