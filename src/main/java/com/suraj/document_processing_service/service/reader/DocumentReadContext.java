package com.suraj.document_processing_service.service.reader;

public record DocumentReadContext(
        String fileName,
        String contentType,
        String sourceBucket,
        String sourceKey,
        String checksum,
        String language
) {
}
