package com.suraj.document_processing_service.dto;

import com.suraj.document_processing_service.enums.DocumentProcessingStatus;
import com.suraj.document_processing_service.enums.DocumentSourceType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        String contentType,
        DocumentSourceType source,
        String sourceBucket,
        String sourceKey,
        String checksum,
        DocumentProcessingStatus processingStatus,
        String language,
        Integer pageCount,
        Integer chunkCount,
        String failureReason,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
