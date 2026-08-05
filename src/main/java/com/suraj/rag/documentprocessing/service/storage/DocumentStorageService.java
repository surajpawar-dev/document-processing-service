package com.suraj.rag.documentprocessing.service.storage;

import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.entity.Document;
import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import com.suraj.rag.documentprocessing.service.chunker.TextChunk;
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
