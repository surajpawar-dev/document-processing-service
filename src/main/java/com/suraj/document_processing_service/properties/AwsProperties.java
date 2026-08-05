package com.suraj.document_processing_service.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

    @NotBlank
    private String region;

    @Valid
    private S3 s3 = new S3();

    @Valid
    private Sqs sqs = new Sqs();

    @Getter
    @Setter
    public static class S3 {
        @NotBlank
        private String bucket;

        private String endpoint;

        private boolean pathStyleAccessEnabled;
    }

    @Getter
    @Setter
    public static class Sqs {
        private String endpoint;

        private String documentReadyQueueUrl;
    }
}