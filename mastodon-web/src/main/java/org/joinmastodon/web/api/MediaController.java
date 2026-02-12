package org.joinmastodon.web.api;

import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.web.api.dto.MediaAttachmentDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/media")
public class MediaController {
    private final MediaAttachmentService mediaAttachmentService;

    public MediaController(MediaAttachmentService mediaAttachmentService) {
        this.mediaAttachmentService = mediaAttachmentService;
    }

    @GetMapping("/{id}")
    public MediaAttachmentDto getMedia(@PathVariable("id") String id) {
        MediaAttachment media = mediaAttachmentService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        return ApiMapper.toMediaAttachmentDto(media);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }
}
