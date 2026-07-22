package com.suraj.document_processing_service.exception;

public class ChunkingException extends DocumentProcessingException {

    public ChunkingException(String message) {
        super(message);
    }

    public ChunkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
