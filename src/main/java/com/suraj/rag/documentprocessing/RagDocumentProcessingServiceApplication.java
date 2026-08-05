package com.suraj.rag.documentprocessing;

import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableAsync
public class RagDocumentProcessingServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(RagDocumentProcessingServiceApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info(
                "Starting rag-document-processing-service timezone={}",
                TimeZone.getDefault().getID());

        SpringApplication.run(RagDocumentProcessingServiceApplication.class, args);
    }
}
