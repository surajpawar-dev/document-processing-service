package com.suraj.rag.documentprocessing.service.event;

import com.suraj.rag.documentprocessing.dto.DocumentReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.events", name = "publisher", havingValue = "logging")
public class NoOpDocumentEventPublisher implements DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpDocumentEventPublisher.class);

    @Override
    public void publishDocumentReady(DocumentReadyEvent event) {
        log.info(
                "Document ready event prepared documentId={} chunkCount={}",
                event.documentId(),
                event.chunkCount());
    }
}
