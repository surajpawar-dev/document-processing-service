package com.suraj.rag.documentprocessing.service.reader;

public record DocumentReadContext(
        String fileName,
        String contentType,
        String sourceBucket,
        String sourceKey,
        String checksum,
        String language) {}
