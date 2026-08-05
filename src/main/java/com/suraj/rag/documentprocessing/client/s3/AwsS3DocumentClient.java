package com.suraj.rag.documentprocessing.client.s3;

import com.suraj.rag.documentprocessing.exception.DocumentReadException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class AwsS3DocumentClient implements S3DocumentClient {

    private static final Logger log = LoggerFactory.getLogger(AwsS3DocumentClient.class);

    private final S3Client s3Client;

    @Override
    public InputStream readObject(String bucket, String key) {
        try {
            log.info("Reading document from S3 bucket={} key={}", bucket, key);
            return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (RuntimeException ex) {
            throw new DocumentReadException("Unable to read document from S3", ex);
        }
    }
}
