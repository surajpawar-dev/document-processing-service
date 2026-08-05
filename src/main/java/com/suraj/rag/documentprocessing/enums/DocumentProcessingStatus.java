package com.suraj.rag.documentprocessing.enums;

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
