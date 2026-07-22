package com.suraj.document_processing_service.exception;

import java.util.UUID;

public class DocumentNotFoundException extends DocumentProcessingException {

    public DocumentNotFoundException(UUID documentId) {
        super("Document not found: " + documentId);
    }
}
