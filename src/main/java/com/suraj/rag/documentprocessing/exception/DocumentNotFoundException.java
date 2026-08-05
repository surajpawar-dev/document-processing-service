package com.suraj.rag.documentprocessing.exception;

import java.util.UUID;

public class DocumentNotFoundException extends DocumentProcessingException {

    public DocumentNotFoundException(UUID documentId) {
        super("Document not found: " + documentId);
    }
}
