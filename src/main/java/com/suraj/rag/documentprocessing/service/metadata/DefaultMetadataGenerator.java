package com.suraj.rag.documentprocessing.service.metadata;

import com.suraj.rag.documentprocessing.entity.Document;
import com.suraj.rag.documentprocessing.service.chunker.TextChunk;
import com.suraj.rag.documentprocessing.util.ChecksumCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultMetadataGenerator implements MetadataGenerator {

    private final ChecksumCalculator checksumCalculator;

    @Override
    public ChunkMetadata generate(Document document, TextChunk chunk) {
        return new ChunkMetadata(
                document.getId(),
                chunk.chunkOrder(),
                chunk.pageNumber(),
                chunk.section(),
                chunk.title(),
                chunk.language() != null ? chunk.language() : document.getLanguage(),
                chunk.source() != null ? chunk.source() : document.getSourceKey(),
                checksumCalculator.sha256(chunk.content()),
                chunk.parentChunkId(),
                chunk.metadata()
        );
    }
}
