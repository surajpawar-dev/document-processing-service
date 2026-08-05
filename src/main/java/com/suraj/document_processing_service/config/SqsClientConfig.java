package com.suraj.document_processing_service.config;

import com.suraj.document_processing_service.properties.AwsProperties;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsClientConfig {

    @Bean
    SqsClient sqsClient(AwsProperties awsProperties) {
        var builder = SqsClient.builder().region(Region.of(awsProperties.getRegion()));

        if (StringUtils.isNotBlank(awsProperties.getSqs().getEndpoint())) {
            builder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }

        return builder.build();
    }
}