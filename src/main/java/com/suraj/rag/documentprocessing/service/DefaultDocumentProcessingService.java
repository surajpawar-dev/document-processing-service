package com.suraj.rag.documentprocessing.service;

import com.suraj.rag.documentprocessing.dto.ChunkResponse;
import com.suraj.rag.documentprocessing.dto.DocumentResponse;
import com.suraj.rag.documentprocessing.dto.DocumentStatusResponse;
import com.suraj.rag.documentprocessing.dto.PagedResponse;
import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.mapper.DocumentMapper;
import com.suraj.rag.documentprocessing.repository.ChunkRepository;
import com.suraj.rag.documentprocessing.service.storage.DocumentStorageService;
import com.suraj.rag.documentprocessing.validation.PdfDocumentRequestValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultDocumentProcessingService implements DocumentProcessingService {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultDocumentProcessingService.class);

    private final DocumentStorageService storageService;
    private final ChunkRepository chunkRepository;
    private final DocumentMapper documentMapper;
    private final PdfDocumentRequestValidator requestValidator;
    private final DocumentProcessingWorker documentProcessingWorker;

    @Override
    public DocumentResponse process(ProcessDocumentRequest request) {
        requestValidator.validate(request);
        var document = storageService.createDocument(request);
        log.info("Document processing accepted documentId={}", document.getId());
        documentProcessingWorker.processAsync(document.getId(), request);
        return documentMapper.toResponse(document);
    }

    @Override
    public DocumentResponse getDocument(UUID documentId) {
        return documentMapper.toResponse(storageService.getDocument(documentId));
    }

    @Override
    public DocumentStatusResponse getStatus(UUID documentId) {
        return documentMapper.toStatusResponse(storageService.getDocument(documentId));
    }

    @Override
    public PagedResponse<ChunkResponse> getChunks(UUID documentId, Pageable pageable) {
        storageService.getDocument(documentId);
        var chunks =
                chunkRepository
                        .findByDocumentId(documentId, pageable)
                        .map(documentMapper::toChunkResponse);
        return PagedResponse.from(chunks);
    }

    @Override
    public DocumentResponse reprocess(UUID documentId) {
        var document = storageService.resetForProcessing(documentId);
        var request =
                new ProcessDocumentRequest(
                        document.getFileName(),
                        document.getSourceBucket(),
                        document.getSourceKey(),
                        document.getChecksum(),
                        document.getLanguage());
        log.info("Document reprocessing accepted documentId={}", document.getId());
        documentProcessingWorker.processAsync(document.getId(), request);
        return documentMapper.toResponse(document);
    }
}
