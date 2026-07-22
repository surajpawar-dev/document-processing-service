package com.suraj.document_processing_service.service.storage;

import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import com.suraj.document_processing_service.entity.Document;
import com.suraj.document_processing_service.entity.DocumentChunk;
import com.suraj.document_processing_service.entity.DocumentProcessingHistory;
import com.suraj.document_processing_service.enums.DocumentProcessingStatus;
import com.suraj.document_processing_service.enums.DocumentSourceType;
import com.suraj.document_processing_service.exception.DocumentNotFoundException;
import com.suraj.document_processing_service.exception.StorageException;
import com.suraj.document_processing_service.repository.ChunkRepository;
import com.suraj.document_processing_service.repository.DocumentRepository;
import com.suraj.document_processing_service.repository.ProcessingHistoryRepository;
import com.suraj.document_processing_service.service.chunker.TextChunk;
import com.suraj.document_processing_service.service.metadata.MetadataGenerator;
import jakarta.transaction.Transactional;
import java.util.List;
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
            document.setContentType("application/pdf");
            document.setSource(DocumentSourceType.S3);
            document.setSourceBucket(request.s3Bucket());
            document.setSourceKey(request.s3Key());
            document.setChecksum(request.checksum());
            document.setLanguage(request.language());
            var saved = documentRepository.save(document);
            addHistory(saved, DocumentProcessingStatus.RECEIVED, "Document processing request received");
            return saved;
        } catch (RuntimeException ex) {
            throw new StorageException("Unable to create document record", ex);
        }
    }

    @Override
    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
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
    public void replaceChunks(Document document, List<TextChunk> chunks) {
        try {
            chunkRepository.deleteByDocumentId(document.getId());
            var entities = chunks.stream()
                    .map(chunk -> toEntity(document, chunk))
                    .toList();
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
