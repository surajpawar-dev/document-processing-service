package com.suraj.document_processing_service.properties;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class ChunkingPropertiesTest {

    @Test
    void rejectsOverlapGreaterThanOrEqualToMaxChunkSize() {
        var properties = new ChunkingProperties();
        properties.setMaxChunkSize(100);
        properties.setOverlapSize(100);

        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var violations = validatorFactory.getValidator().validate(properties);

            assertThat(violations)
                    .anySatisfy(violation -> assertThat(violation.getMessage())
                            .isEqualTo("overlapSize must be smaller than maxChunkSize"));
        }
    }

    @Test
    void rejectsMinChunkSizeGreaterThanMaxChunkSize() {
        var properties = new ChunkingProperties();
        properties.setMaxChunkSize(100);
        properties.setMinChunkSize(101);

        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var violations = validatorFactory.getValidator().validate(properties);

            assertThat(violations)
                    .anySatisfy(violation -> assertThat(violation.getMessage())
                            .isEqualTo("minChunkSize must be less than or equal to maxChunkSize"));
        }
    }
}
