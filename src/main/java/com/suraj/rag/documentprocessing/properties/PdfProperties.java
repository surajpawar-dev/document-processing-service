package com.suraj.rag.documentprocessing.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.pdf")
public class PdfProperties {

    @Positive
    private long maxFileSizeBytes = 52_428_800;

    private boolean passwordProtectedSupported;

    @NotNull
    private Duration extractionTimeout = Duration.ofMinutes(2);
}
