package com.suraj.document_processing_service.service.chunker;

import com.suraj.document_processing_service.enums.ChunkingStrategyType;
import com.suraj.document_processing_service.exception.ChunkingException;
import com.suraj.document_processing_service.properties.ChunkingProperties;
import com.suraj.document_processing_service.service.cleaner.CleanedDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecursiveChunkingStrategy implements ChunkingStrategy {

    private final ChunkingProperties properties;

    @Override
    public ChunkingStrategyType type() {
        return ChunkingStrategyType.RECURSIVE;
    }

    @Override
    public List<TextChunk> chunk(CleanedDocument document) {
        try {
            // TODO implement recursive chunking using properties.getMaxChunkSize() and overlap settings.
            return List.of();
        } catch (RuntimeException ex) {
            throw new ChunkingException("Unable to chunk document recursively", ex);
        }
    }
}
