package com.suraj.rag.documentprocessing.properties;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.chunking")
public class ChunkingProperties {

    @NotNull
    private ChunkingStrategyType strategy = ChunkingStrategyType.RECURSIVE;

    @Min(1)
    private int maxChunkSize = 1000;

    @Min(0)
    private int overlapSize = 200;

    @Min(1)
    private int minChunkSize = 200;

    @AssertTrue(message = "overlapSize must be smaller than maxChunkSize")
    boolean isOverlapSmallerThanMaxChunkSize() {
        return overlapSize < maxChunkSize;
    }

    @AssertTrue(message = "minChunkSize must be less than or equal to maxChunkSize")
    boolean isMinChunkSizeWithinMaxChunkSize() {
        return minChunkSize <= maxChunkSize;
    }
}
