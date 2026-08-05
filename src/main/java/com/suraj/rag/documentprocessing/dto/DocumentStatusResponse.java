package com.suraj.rag.documentprocessing.dto;

import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import java.time.Instant;
import java.util.UUID;

public record DocumentStatusResponse(
        UUID documentId,
        DocumentProcessingStatus status,
        String failureReason,
        Instant updatedAt
) {
}
