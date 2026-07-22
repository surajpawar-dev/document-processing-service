package com.suraj.document_processing_service.enums;

public enum DocumentProcessingStatus {
    RECEIVED,
    READING,
    CLEANING,
    CHUNKING,
    METADATA_GENERATION,
    STORING,
    READY,
    FAILED
}
