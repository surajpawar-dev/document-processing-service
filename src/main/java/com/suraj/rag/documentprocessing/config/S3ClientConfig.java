package com.suraj.rag.documentprocessing.config;

import com.suraj.rag.documentprocessing.properties.AwsProperties;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3ClientConfig {

    @Bean
    S3Client s3Client(AwsProperties awsProperties) {
        var builder =
                S3Client.builder()
                        .region(Region.of(awsProperties.getRegion()))
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(
                                                awsProperties.getS3().isPathStyleAccessEnabled())
                                        .build());

        if (StringUtils.isNotBlank(awsProperties.getS3().getEndpoint())) {
            builder.endpointOverride(URI.create(awsProperties.getS3().getEndpoint()));
        }

        return builder.build();
    }
}
