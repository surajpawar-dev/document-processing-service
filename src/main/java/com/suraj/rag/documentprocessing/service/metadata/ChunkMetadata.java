package com.suraj.rag.documentprocessing.service.metadata;

import java.util.Map;
import java.util.UUID;

public record ChunkMetadata(
        UUID documentId,
        int chunkOrder,
        Integer pageNumber,
        String section,
        String title,
        String language,
        String source,
        String chunkChecksum,
        UUID parentChunkId,
        Map<String, Object> metadata
) {
}
