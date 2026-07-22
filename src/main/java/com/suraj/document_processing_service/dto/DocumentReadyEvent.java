package com.suraj.document_processing_service.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentReadyEvent(
        UUID documentId,
        String checksum,
        Integer chunkCount,
        Instant readyAt
) {
}
