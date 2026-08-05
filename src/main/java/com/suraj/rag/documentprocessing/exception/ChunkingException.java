package com.suraj.rag.documentprocessing.exception;

public class ChunkingException extends DocumentProcessingException {

    public ChunkingException(String message) {
        super(message);
    }

    public ChunkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
