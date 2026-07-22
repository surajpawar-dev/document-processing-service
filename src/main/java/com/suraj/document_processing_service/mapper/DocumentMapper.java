package com.suraj.document_processing_service.mapper;

import com.suraj.document_processing_service.dto.ChunkResponse;
import com.suraj.document_processing_service.dto.DocumentResponse;
import com.suraj.document_processing_service.dto.DocumentStatusResponse;
import com.suraj.document_processing_service.entity.Document;
import com.suraj.document_processing_service.entity.DocumentChunk;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getSource(),
                document.getSourceBucket(),
                document.getSourceKey(),
                document.getChecksum(),
                document.getProcessingStatus(),
                document.getLanguage(),
                document.getPageCount(),
                document.getChunkCount(),
                document.getFailureReason(),
                document.getMetadata(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getVersion()
        );
    }

    public DocumentStatusResponse toStatusResponse(Document document) {
        return new DocumentStatusResponse(
                document.getId(),
                document.getProcessingStatus(),
                document.getFailureReason(),
                document.getUpdatedAt()
        );
    }

    public ChunkResponse toChunkResponse(DocumentChunk chunk) {
        return new ChunkResponse(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getChunkOrder(),
                chunk.getContent(),
                chunk.getChecksum(),
                chunk.getPageNumber(),
                chunk.getSection(),
                chunk.getTitle(),
                chunk.getLanguage(),
                chunk.getSource(),
                chunk.getParentChunkId(),
                chunk.getMetadata(),
                chunk.getCreatedAt()
        );
    }
}
