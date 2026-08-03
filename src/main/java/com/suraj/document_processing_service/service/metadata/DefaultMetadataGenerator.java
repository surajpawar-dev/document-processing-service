package com.suraj.document_processing_service.service.metadata;

import com.suraj.document_processing_service.entity.Document;
import com.suraj.document_processing_service.service.chunker.TextChunk;
import com.suraj.document_processing_service.util.ChecksumCalculator;
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
