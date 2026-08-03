package com.suraj.document_processing_service.service.event;

import com.suraj.document_processing_service.dto.DocumentReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpDocumentEventPublisher implements DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpDocumentEventPublisher.class);

    @Override
    public void publishDocumentReady(DocumentReadyEvent event) {
        log.info("Document ready event prepared documentId={} chunkCount={}", event.documentId(), event.chunkCount());
    }
}
