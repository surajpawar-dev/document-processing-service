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
public class SentenceChunkingStrategy implements ChunkingStrategy {

    private final ChunkingProperties properties;

    @Override
    public ChunkingStrategyType type() {
        return ChunkingStrategyType.SENTENCE;
    }

    @Override
    public List<TextChunk> chunk(CleanedDocument document) {
        try {
            // TODO implement sentence-aware chunking using configured size limits.
            return List.of();
        } catch (RuntimeException ex) {
            throw new ChunkingException("Unable to chunk document by sentence", ex);
        }
    }
}
