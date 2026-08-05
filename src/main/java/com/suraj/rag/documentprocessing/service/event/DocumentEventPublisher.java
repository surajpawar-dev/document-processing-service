package com.suraj.rag.documentprocessing.service.event;

import com.suraj.rag.documentprocessing.dto.DocumentReadyEvent;

public interface DocumentEventPublisher {

    void publishDocumentReady(DocumentReadyEvent event);
}
