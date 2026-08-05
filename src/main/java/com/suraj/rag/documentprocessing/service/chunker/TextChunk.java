package com.suraj.rag.documentprocessing.service.chunker;

import java.util.Map;
import java.util.UUID;

public record TextChunk(
        int chunkOrder,
        String content,
        Integer pageNumber,
        String section,
        String title,
        String language,
        String source,
        UUID parentChunkId,
        Map<String, Object> metadata) {}
