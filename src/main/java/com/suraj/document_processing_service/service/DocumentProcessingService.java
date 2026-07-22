package com.suraj.document_processing_service.service;

import com.suraj.document_processing_service.dto.ChunkResponse;
import com.suraj.document_processing_service.dto.DocumentResponse;
import com.suraj.document_processing_service.dto.DocumentStatusResponse;
import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import java.util.List;
import java.util.UUID;

public interface DocumentProcessingService {

    DocumentResponse process(ProcessDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    DocumentStatusResponse getStatus(UUID documentId);

    List<ChunkResponse> getChunks(UUID documentId);

    DocumentResponse reprocess(UUID documentId);
}
