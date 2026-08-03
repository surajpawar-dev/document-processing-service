package com.suraj.document_processing_service.service;

import com.suraj.document_processing_service.dto.ChunkResponse;
import com.suraj.document_processing_service.dto.DocumentResponse;
import com.suraj.document_processing_service.dto.DocumentStatusResponse;
import com.suraj.document_processing_service.dto.PagedResponse;
import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface DocumentProcessingService {

    DocumentResponse process(ProcessDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    DocumentStatusResponse getStatus(UUID documentId);

    PagedResponse<ChunkResponse> getChunks(UUID documentId, Pageable pageable);

    DocumentResponse reprocess(UUID documentId);
}
