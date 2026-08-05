package com.suraj.rag.documentprocessing.service.chunker;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import com.suraj.rag.documentprocessing.exception.ChunkingException;
import com.suraj.rag.documentprocessing.properties.ChunkingProperties;
import com.suraj.rag.documentprocessing.service.cleaner.CleanedDocument;
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
