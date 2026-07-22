package com.suraj.document_processing_service.service;

import com.suraj.document_processing_service.client.s3.S3DocumentClient;
import com.suraj.document_processing_service.dto.ChunkResponse;
import com.suraj.document_processing_service.dto.DocumentReadyEvent;
import com.suraj.document_processing_service.dto.DocumentResponse;
import com.suraj.document_processing_service.dto.DocumentStatusResponse;
import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import com.suraj.document_processing_service.enums.DocumentProcessingStatus;
import com.suraj.document_processing_service.mapper.DocumentMapper;
import com.suraj.document_processing_service.properties.ChunkingProperties;
import com.suraj.document_processing_service.repository.ChunkRepository;
import com.suraj.document_processing_service.service.chunker.ChunkingStrategyResolver;
import com.suraj.document_processing_service.service.cleaner.TextCleaner;
import com.suraj.document_processing_service.service.event.DocumentEventPublisher;
import com.suraj.document_processing_service.service.reader.DocumentReadContext;
import com.suraj.document_processing_service.service.reader.DocumentReader;
import com.suraj.document_processing_service.service.storage.DocumentStorageService;
import com.suraj.document_processing_service.validation.PdfDocumentRequestValidator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultDocumentProcessingService implements DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DefaultDocumentProcessingService.class);

    private final S3DocumentClient s3DocumentClient;
    private final DocumentReader documentReader;
    private final TextCleaner textCleaner;
    private final ChunkingStrategyResolver chunkingStrategyResolver;
    private final ChunkingProperties chunkingProperties;
    private final DocumentStorageService storageService;
    private final ChunkRepository chunkRepository;
    private final DocumentEventPublisher eventPublisher;
    private final DocumentMapper documentMapper;
    private final PdfDocumentRequestValidator requestValidator;

    @Override
    public DocumentResponse process(ProcessDocumentRequest request) {
        requestValidator.validate(request);
        var document = storageService.createDocument(request);
        try {
            log.info("Starting document processing documentId={}", document.getId());
            storageService.updateStatus(document.getId(), DocumentProcessingStatus.READING, "Reading source document");
            var inputStream = s3DocumentClient.readObject(request.s3Bucket(), request.s3Key());
            var readDocument = documentReader.read(inputStream, new DocumentReadContext(
                    request.fileName(),
                    "application/pdf",
                    request.s3Bucket(),
                    request.s3Key(),
                    request.checksum(),
                    request.language()
            ));

            storageService.updateStatus(document.getId(), DocumentProcessingStatus.CLEANING, "Cleaning extracted text");
            var cleanedDocument = textCleaner.clean(readDocument);

            storageService.updateStatus(document.getId(), DocumentProcessingStatus.CHUNKING, "Chunking cleaned text");
            var chunks = chunkingStrategyResolver.resolve(chunkingProperties.getStrategy()).chunk(cleanedDocument);

            storageService.updateStatus(document.getId(), DocumentProcessingStatus.STORING, "Storing chunks");
            storageService.replaceChunks(document, chunks);

            var ready = storageService.updateStatus(document.getId(), DocumentProcessingStatus.READY, "Document is ready for downstream consumption");
            eventPublisher.publishDocumentReady(new DocumentReadyEvent(
                    ready.getId(),
                    ready.getChecksum(),
                    ready.getChunkCount(),
                    Instant.now()
            ));
            return documentMapper.toResponse(ready);
        } catch (RuntimeException ex) {
            log.error("Document processing failed documentId={}", document.getId(), ex);
            var failed = storageService.updateStatus(document.getId(), DocumentProcessingStatus.FAILED, ex.getMessage());
            return documentMapper.toResponse(failed);
        }
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
    public List<ChunkResponse> getChunks(UUID documentId) {
        storageService.getDocument(documentId);
        return chunkRepository.findByDocumentIdOrderByChunkOrderAsc(documentId).stream()
                .map(documentMapper::toChunkResponse)
                .toList();
    }

    @Override
    public DocumentResponse reprocess(UUID documentId) {
        var document = storageService.getDocument(documentId);
        return process(new ProcessDocumentRequest(
                document.getFileName(),
                document.getSourceBucket(),
                document.getSourceKey(),
                document.getChecksum(),
                document.getLanguage()
        ));
    }
}
