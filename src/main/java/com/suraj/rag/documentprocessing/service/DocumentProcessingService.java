package com.suraj.rag.documentprocessing.service;

import com.suraj.rag.documentprocessing.dto.ChunkResponse;
import com.suraj.rag.documentprocessing.dto.DocumentResponse;
import com.suraj.rag.documentprocessing.dto.DocumentStatusResponse;
import com.suraj.rag.documentprocessing.dto.PagedResponse;
import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface DocumentProcessingService {

    DocumentResponse process(ProcessDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    DocumentStatusResponse getStatus(UUID documentId);

    PagedResponse<ChunkResponse> getChunks(UUID documentId, Pageable pageable);

    DocumentResponse reprocess(UUID documentId);
}
