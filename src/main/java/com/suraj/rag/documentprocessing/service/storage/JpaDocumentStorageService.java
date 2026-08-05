package com.suraj.rag.documentprocessing.service.storage;

import com.suraj.rag.documentprocessing.constants.ApplicationConstants;
import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.entity.Document;
import com.suraj.rag.documentprocessing.entity.DocumentChunk;
import com.suraj.rag.documentprocessing.entity.DocumentProcessingHistory;
import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import com.suraj.rag.documentprocessing.enums.DocumentSourceType;
import com.suraj.rag.documentprocessing.exception.DocumentNotFoundException;
import com.suraj.rag.documentprocessing.exception.StorageException;
import com.suraj.rag.documentprocessing.repository.ChunkRepository;
import com.suraj.rag.documentprocessing.repository.DocumentRepository;
import com.suraj.rag.documentprocessing.repository.ProcessingHistoryRepository;
import com.suraj.rag.documentprocessing.service.chunker.TextChunk;
import com.suraj.rag.documentprocessing.service.metadata.MetadataGenerator;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaDocumentStorageService implements DocumentStorageService {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final ProcessingHistoryRepository processingHistoryRepository;
    private final MetadataGenerator metadataGenerator;

    @Override
    @Transactional
    public Document createDocument(ProcessDocumentRequest request) {
        try {
            var document = new Document();
            document.setFileName(request.fileName());
            document.setContentType(ApplicationConstants.PDF_CONTENT_TYPE);
            document.setSource(DocumentSourceType.S3);
            document.setSourceBucket(request.s3Bucket());
            document.setSourceKey(request.s3Key());
            document.setChecksum(request.checksum());
            document.setLanguage(request.language());
            var saved = documentRepository.save(document);
            addHistory(
                    saved, DocumentProcessingStatus.RECEIVED, ApplicationConstants.STATUS_RECEIVED);
            return saved;
        } catch (RuntimeException ex) {
            throw new StorageException("Unable to create document record", ex);
        }
    }

    @Override
    public Document getDocument(UUID documentId) {
        return documentRepository
                .findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    @Override
    @Transactional
    public Document updateStatus(UUID documentId, DocumentProcessingStatus status, String message) {
        var document = getDocument(documentId);
        document.setProcessingStatus(status);
        if (status == DocumentProcessingStatus.FAILED) {
            document.setFailureReason(message);
        }
        addHistory(document, status, message);
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public Document updateReadMetadata(
            UUID documentId, Integer pageCount, Map<String, Object> metadata) {
        var document = getDocument(documentId);
        document.setPageCount(pageCount);
        if (metadata != null && !metadata.isEmpty()) {
            document.setMetadata(new HashMap<>(metadata));
        }
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public Document resetForProcessing(UUID documentId) {
        var document = getDocument(documentId);
        chunkRepository.deleteByDocumentId(documentId);
        document.setProcessingStatus(DocumentProcessingStatus.RECEIVED);
        document.setFailureReason(null);
        document.setChunkCount(0);
        addHistory(
                document, DocumentProcessingStatus.RECEIVED, ApplicationConstants.STATUS_RECEIVED);
        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public void replaceChunks(Document document, List<TextChunk> chunks) {
        try {
            chunkRepository.deleteByDocumentId(document.getId());
            var entities = chunks.stream().map(chunk -> toEntity(document, chunk)).toList();
            chunkRepository.saveAll(entities);
            document.setChunkCount(entities.size());
            documentRepository.save(document);
        } catch (RuntimeException ex) {
            throw new StorageException("Unable to store document chunks", ex);
        }
    }

    private DocumentChunk toEntity(Document document, TextChunk chunk) {
        var metadata = metadataGenerator.generate(document, chunk);
        var entity = new DocumentChunk();
        entity.setDocument(document);
        entity.setChunkOrder(metadata.chunkOrder());
        entity.setContent(chunk.content());
        entity.setChecksum(metadata.chunkChecksum());
        entity.setPageNumber(metadata.pageNumber());
        entity.setSection(metadata.section());
        entity.setTitle(metadata.title());
        entity.setLanguage(metadata.language());
        entity.setSource(metadata.source());
        entity.setParentChunkId(metadata.parentChunkId());
        entity.setMetadata(metadata.metadata());
        return entity;
    }

    private void addHistory(Document document, DocumentProcessingStatus status, String message) {
        var history = new DocumentProcessingHistory();
        history.setDocument(document);
        history.setStatus(status);
        history.setMessage(message);
        processingHistoryRepository.save(history);
    }
}
