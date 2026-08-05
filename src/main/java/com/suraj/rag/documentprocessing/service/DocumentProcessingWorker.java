package com.suraj.rag.documentprocessing.service;

import com.suraj.rag.documentprocessing.client.s3.S3DocumentClient;
import com.suraj.rag.documentprocessing.config.AsyncProcessingConfig;
import com.suraj.rag.documentprocessing.constants.ApplicationConstants;
import com.suraj.rag.documentprocessing.dto.DocumentReadyEvent;
import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import com.suraj.rag.documentprocessing.exception.DocumentReadException;
import com.suraj.rag.documentprocessing.properties.ChunkingProperties;
import com.suraj.rag.documentprocessing.service.chunker.ChunkingStrategyResolver;
import com.suraj.rag.documentprocessing.service.cleaner.TextCleaner;
import com.suraj.rag.documentprocessing.service.event.DocumentEventPublisher;
import com.suraj.rag.documentprocessing.service.reader.DocumentReadContext;
import com.suraj.rag.documentprocessing.service.reader.DocumentReader;
import com.suraj.rag.documentprocessing.service.storage.DocumentStorageService;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentProcessingWorker {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingWorker.class);

    private final S3DocumentClient s3DocumentClient;
    private final DocumentReader documentReader;
    private final TextCleaner textCleaner;
    private final ChunkingStrategyResolver chunkingStrategyResolver;
    private final ChunkingProperties chunkingProperties;
    private final DocumentStorageService storageService;
    private final DocumentEventPublisher eventPublisher;

    @Async(AsyncProcessingConfig.DOCUMENT_PROCESSING_EXECUTOR)
    public void processAsync(UUID documentId, ProcessDocumentRequest request) {
        try {
            process(documentId, request);
        } catch (RuntimeException ex) {
            log.error("Document processing failed documentId={}", documentId, ex);
            storageService.updateStatus(documentId, DocumentProcessingStatus.FAILED, failureMessage(ex));
        }
    }

    void process(UUID documentId, ProcessDocumentRequest request) {
        log.info("Document processing started documentId={} sourceBucket={} sourceKey={}",
                documentId, request.s3Bucket(), request.s3Key());

        storageService.updateStatus(documentId, DocumentProcessingStatus.READING, ApplicationConstants.STATUS_READING);
        var readContext = new DocumentReadContext(
                request.fileName(),
                ApplicationConstants.PDF_CONTENT_TYPE,
                request.s3Bucket(),
                request.s3Key(),
                request.checksum(),
                request.language()
        );
        var readDocument = readDocument(request, readContext);
        storageService.updateReadMetadata(documentId, pageCount(readDocument.metadata()), readDocument.metadata());

        storageService.updateStatus(documentId, DocumentProcessingStatus.CLEANING, ApplicationConstants.STATUS_CLEANING);
        var cleanedDocument = textCleaner.clean(readDocument);

        storageService.updateStatus(documentId, DocumentProcessingStatus.CHUNKING, ApplicationConstants.STATUS_CHUNKING);
        var chunks = chunkingStrategyResolver.resolve(chunkingProperties.getStrategy()).chunk(cleanedDocument);

        storageService.updateStatus(documentId, DocumentProcessingStatus.STORING, ApplicationConstants.STATUS_STORING);
        var document = storageService.getDocument(documentId);
        storageService.replaceChunks(document, chunks);

        var ready = storageService.updateStatus(documentId, DocumentProcessingStatus.READY, ApplicationConstants.STATUS_READY);
        eventPublisher.publishDocumentReady(new DocumentReadyEvent(
                ready.getId(),
                ready.getChecksum(),
                ready.getChunkCount(),
                Instant.now()
        ));
        log.info("Document processing completed documentId={} chunkCount={}", ready.getId(), ready.getChunkCount());
    }

    private com.suraj.rag.documentprocessing.service.reader.ReadDocument readDocument(
            ProcessDocumentRequest request,
            DocumentReadContext readContext
    ) {
        try (var inputStream = s3DocumentClient.readObject(request.s3Bucket(), request.s3Key())) {
            return documentReader.read(inputStream, readContext);
        } catch (IOException ex) {
            throw new DocumentReadException("Unable to close source document stream", ex);
        }
    }

    private Integer pageCount(Map<String, Object> metadata) {
        var value = metadata.get("pageCount");
        return value instanceof Number number ? number.intValue() : null;
    }

    private String failureMessage(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? ApplicationConstants.STATUS_FAILED
                : ex.getMessage();
    }
}
