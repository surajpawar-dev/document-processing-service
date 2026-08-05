package com.suraj.document_processing_service.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suraj.document_processing_service.dto.DocumentReadyEvent;
import com.suraj.document_processing_service.properties.AwsProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(prefix = "app.events", name = "publisher", havingValue = "sqs", matchIfMissing = true)
public class SqsDocumentEventPublisher implements DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsDocumentEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AwsProperties awsProperties;

    public SqsDocumentEventPublisher(SqsClient sqsClient, ObjectMapper objectMapper, AwsProperties awsProperties) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.awsProperties = awsProperties;
    }

    @Override
    public void publishDocumentReady(DocumentReadyEvent event) {
        String queueUrl = awsProperties.getSqs().getDocumentReadyQueueUrl();
        if (StringUtils.isBlank(queueUrl)) {
            throw new IllegalStateException("Document ready SQS queue URL is not configured");
        }

        String body = toJson(event);
        var requestBuilder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body);

        if (queueUrl.endsWith(".fifo")) {
            requestBuilder.messageGroupId(event.documentId().toString())
                    .messageDeduplicationId(event.documentId() + ":" + event.checksum());
        }

        sqsClient.sendMessage(requestBuilder.build());
        log.info("Published document ready event documentId={} chunkCount={}", event.documentId(), event.chunkCount());
    }

    private String toJson(DocumentReadyEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize document ready event", ex);
        }
    }
}