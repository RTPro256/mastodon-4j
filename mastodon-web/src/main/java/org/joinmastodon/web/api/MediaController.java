package org.joinmastodon.web.api;

import java.io.IOException;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.MediaAttachmentService;
import org.joinmastodon.media.processing.MediaIngestionService;
import org.joinmastodon.media.scanning.AvScannerException;
import org.joinmastodon.web.api.dto.MediaAttachmentDto;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/media")
public class MediaController {
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaIngestionService mediaIngestionService;
    private final AccountService accountService;

    public MediaController(MediaAttachmentService mediaAttachmentService,
                           MediaIngestionService mediaIngestionService,
                           AccountService accountService) {
        this.mediaAttachmentService = mediaAttachmentService;
        this.mediaIngestionService = mediaIngestionService;
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public MediaAttachmentDto getMedia(@PathVariable("id") String id) {
        MediaAttachment media = mediaAttachmentService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        return ApiMapper.toMediaAttachmentDto(media);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaAttachmentDto upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "async", required = false) Boolean async) {
        Account account = requireAccount();
        try {
            MediaAttachment attachment = mediaIngestionService.ingest(
                    account.getId(),
                    file,
                    description,
                    Boolean.TRUE.equals(async));
            return ApiMapper.toMediaAttachmentDto(attachment);
        } catch (IOException | AvScannerException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public MediaAttachmentDto update(
            @PathVariable("id") String id,
            @RequestParam(value = "description", required = false) String description) {
        Account account = requireAccount();
        MediaAttachment attachment = mediaAttachmentService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        if (!attachment.getAccountId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        attachment.setDescription(description);
        MediaAttachment saved = mediaAttachmentService.save(attachment);
        return ApiMapper.toMediaAttachmentDto(saved);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    private Account requireAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.accountId() != null) {
                return accountService.findById(principal.accountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
