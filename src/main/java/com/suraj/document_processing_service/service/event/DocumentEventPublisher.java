package com.suraj.document_processing_service.service.event;

import com.suraj.document_processing_service.dto.DocumentReadyEvent;

public interface DocumentEventPublisher {

    void publishDocumentReady(DocumentReadyEvent event);
}
