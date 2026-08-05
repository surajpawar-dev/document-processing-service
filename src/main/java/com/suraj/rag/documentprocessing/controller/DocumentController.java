package com.suraj.rag.documentprocessing.controller;

import com.suraj.rag.documentprocessing.constants.ApplicationConstants;
import com.suraj.rag.documentprocessing.dto.ChunkResponse;
import com.suraj.rag.documentprocessing.dto.DocumentResponse;
import com.suraj.rag.documentprocessing.dto.DocumentStatusResponse;
import com.suraj.rag.documentprocessing.dto.PagedResponse;
import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.service.DocumentProcessingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(ApplicationConstants.API_BASE_PATH)
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping(ApplicationConstants.PROCESS_PATH)
    public ResponseEntity<DocumentResponse> process(@Valid @RequestBody ProcessDocumentRequest request) {
        var response = documentProcessingService.process(request);
        return accepted(response);
    }

    @GetMapping(ApplicationConstants.DOCUMENT_ID_PATH)
    public DocumentResponse getDocument(@PathVariable UUID id) {
        return documentProcessingService.getDocument(id);
    }

    @GetMapping(ApplicationConstants.STATUS_PATH)
    public DocumentStatusResponse getStatus(@PathVariable UUID id) {
        return documentProcessingService.getStatus(id);
    }

    @GetMapping(ApplicationConstants.CHUNKS_PATH)
    public PagedResponse<ChunkResponse> getChunks(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(ApplicationConstants.MAX_CHUNK_PAGE_SIZE) int size
    ) {
        return documentProcessingService.getChunks(id, chunkPageRequest(page, size));
    }

    @PostMapping(ApplicationConstants.REPROCESS_PATH)
    public ResponseEntity<DocumentResponse> reprocess(@PathVariable UUID id) {
        var response = documentProcessingService.reprocess(id);
        return accepted(response);
    }

    private ResponseEntity<DocumentResponse> accepted(DocumentResponse response) {
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(ApplicationConstants.API_BASE_PATH)
                .path(ApplicationConstants.DOCUMENT_ID_PATH)
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(location)
                .body(response);
    }

    private Pageable chunkPageRequest(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, ApplicationConstants.CHUNK_ORDER_FIELD)
        );
    }
}
