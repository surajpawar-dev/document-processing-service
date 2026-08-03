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
public class FixedChunkingStrategy implements ChunkingStrategy {

    private final ChunkingProperties properties;

    @Override
    public ChunkingStrategyType type() {
        return ChunkingStrategyType.FIXED;
    }

    @Override
    public List<TextChunk> chunk(CleanedDocument document) {
        try {
            var text = ChunkingSupport.normalizeText(document.text());
            var chunks = ChunkingSupport.fixedWindows(text, properties);
            return ChunkingSupport.toChunks(chunks, text, type());
        } catch (RuntimeException ex) {
            throw new ChunkingException("Unable to chunk document using fixed-size strategy", ex);
        }
    }
}
