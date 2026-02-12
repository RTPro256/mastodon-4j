package org.joinmastodon.web.api;

import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.service.StatusService;
import org.joinmastodon.web.api.dto.StatusDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/statuses")
public class StatusController {
    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/{id}")
    public StatusDto getStatus(@PathVariable("id") String id) {
        Status status = statusService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        return ApiMapper.toStatusDto(status);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }
}
