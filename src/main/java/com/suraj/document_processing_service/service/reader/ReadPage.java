package com.suraj.document_processing_service.service.reader;

import java.util.Map;

public record ReadPage(
        int pageNumber,
        String text,
        Map<String, Object> metadata
) {
}
