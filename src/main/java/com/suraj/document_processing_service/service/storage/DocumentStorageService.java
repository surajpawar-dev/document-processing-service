package com.suraj.document_processing_service.service.storage;

import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import com.suraj.document_processing_service.entity.Document;
import com.suraj.document_processing_service.enums.DocumentProcessingStatus;
import com.suraj.document_processing_service.service.chunker.TextChunk;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DocumentStorageService {

    Document createDocument(ProcessDocumentRequest request);

    Document getDocument(UUID documentId);

    Document updateStatus(UUID documentId, DocumentProcessingStatus status, String message);

    Document updateReadMetadata(UUID documentId, Integer pageCount, Map<String, Object> metadata);

    Document resetForProcessing(UUID documentId);

    void replaceChunks(Document document, List<TextChunk> chunks);
}
