package com.suraj.document_processing_service.controller;

import com.suraj.document_processing_service.constants.ApplicationConstants;
import com.suraj.document_processing_service.dto.ChunkResponse;
import com.suraj.document_processing_service.dto.DocumentResponse;
import com.suraj.document_processing_service.dto.DocumentStatusResponse;
import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import com.suraj.document_processing_service.service.DocumentProcessingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApplicationConstants.API_BASE_PATH)
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DocumentResponse process(@Valid @RequestBody ProcessDocumentRequest request) {
        return documentProcessingService.process(request);
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable UUID id) {
        return documentProcessingService.getDocument(id);
    }

    @GetMapping("/{id}/status")
    public DocumentStatusResponse getStatus(@PathVariable UUID id) {
        return documentProcessingService.getStatus(id);
    }

    @GetMapping("/{id}/chunks")
    public List<ChunkResponse> getChunks(@PathVariable UUID id) {
        return documentProcessingService.getChunks(id);
    }

    @PostMapping("/reprocess/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DocumentResponse reprocess(@PathVariable UUID id) {
        return documentProcessingService.reprocess(id);
    }
}
