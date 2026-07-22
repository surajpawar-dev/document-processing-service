package com.suraj.document_processing_service.service.cleaner;

import java.util.Map;

public record CleanedDocument(
        String text,
        Map<String, Object> metadata
) {
}
