package com.suraj.rag.documentprocessing.dto;

import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import com.suraj.rag.documentprocessing.enums.DocumentSourceType;
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
        Long version) {}
