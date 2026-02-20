package org.joinmastodon.web.api.admin;

import java.util.List;
import org.joinmastodon.core.entity.DomainBlock;
import org.joinmastodon.core.service.ModerationService;
import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.auth.AdminOnly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/admin/domain_blocks")
@AdminOnly
public class AdminDomainBlockController {

    private final ModerationService moderationService;

    public AdminDomainBlockController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping
    public ResponseEntity<List<DomainBlockDto>> listDomainBlocks(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "max_id", required = false) String maxId,
            @RequestParam(value = "since_id", required = false) String sinceId) {
        
        int resolvedLimit = limit == null ? 100 : Math.min(Math.max(limit, 1), 200);
        Pageable pageable = PageRequest.of(0, resolvedLimit, Sort.by("createdAt").descending());
        
        Page<DomainBlock> domainBlocks = moderationService.getDomainBlocks(pageable);
        
        List<DomainBlockDto> result = domainBlocks.getContent().stream()
                .map(this::toDomainBlockDto)
                .toList();
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainBlockDto> getDomainBlock(@PathVariable("id") String id) {
        DomainBlock domainBlock = moderationService.getDomainBlock(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Domain block not found"));
        return ResponseEntity.ok(toDomainBlockDto(domainBlock));
    }

    @PostMapping
    public ResponseEntity<DomainBlockDto> createDomainBlock(@RequestBody DomainBlockRequest request) {
        if (request.domain() == null || request.domain().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }
        
        DomainBlock.Severity severity = request.severity() != null 
                ? DomainBlock.Severity.valueOf(request.severity().toUpperCase())
                : DomainBlock.Severity.SILENCE;
        
        DomainBlock domainBlock = moderationService.blockDomain(
                request.domain(),
                severity,
                request.rejectMedia() != null ? request.rejectMedia() : false,
                request.rejectReports() != null ? request.rejectReports() : false,
                request.privateComment(),
                request.publicComment(),
                request.obfuscate() != null ? request.obfuscate() : false
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toDomainBlockDto(domainBlock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomainBlock(@PathVariable("id") String id) {
        DomainBlock domainBlock = moderationService.getDomainBlock(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Domain block not found"));
        
        moderationService.unblockDomain(domainBlock.getId());
        
        return ResponseEntity.noContent().build();
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    private DomainBlockDto toDomainBlockDto(DomainBlock domainBlock) {
        return new DomainBlockDto(
                String.valueOf(domainBlock.getId()),
                domainBlock.getDomain(),
                domainBlock.getCreatedAt(),
                domainBlock.getSeverity().name().toLowerCase(),
                domainBlock.isRejectMedia(),
                domainBlock.isRejectReports(),
                domainBlock.getPrivateComment(),
                domainBlock.getPublicComment(),
                domainBlock.isObfuscate()
        );
    }

    // DTOs
    public record DomainBlockRequest(
            String domain,
            String severity,
            Boolean rejectMedia,
            Boolean rejectReports,
            String privateComment,
            String publicComment,
            Boolean obfuscate) {}

    public record DomainBlockDto(
            String id,
            String domain,
            java.time.Instant createdAt,
            String severity,
            boolean rejectMedia,
            boolean rejectReports,
            String privateComment,
            String publicComment,
            boolean obfuscate) {}
}
