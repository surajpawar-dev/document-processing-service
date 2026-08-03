package com.suraj.document_processing_service;

import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableAsync
public class DocumentProcessingServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(DocumentProcessingServiceApplication.class);

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		log.info("Starting document-processing-service timezone={}", TimeZone.getDefault().getID());

		SpringApplication.run(DocumentProcessingServiceApplication.class, args);
	}
}
