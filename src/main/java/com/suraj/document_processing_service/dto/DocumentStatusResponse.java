package com.suraj.document_processing_service.dto;

import com.suraj.document_processing_service.enums.DocumentProcessingStatus;
import java.time.Instant;
import java.util.UUID;

public record DocumentStatusResponse(
        UUID documentId,
        DocumentProcessingStatus status,
        String failureReason,
        Instant updatedAt
) {
}
