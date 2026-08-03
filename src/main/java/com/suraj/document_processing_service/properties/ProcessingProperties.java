package com.suraj.document_processing_service.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.processing")
public class ProcessingProperties {

    @Min(1)
    private int corePoolSize = 2;

    @Min(1)
    private int maxPoolSize = 4;

    @Min(1)
    private int queueCapacity = 100;

    @NotBlank
    private String threadNamePrefix = "document-processing-";
}
