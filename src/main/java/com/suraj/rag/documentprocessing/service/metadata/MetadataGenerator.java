package com.suraj.rag.documentprocessing.service.metadata;

import com.suraj.rag.documentprocessing.entity.Document;
import com.suraj.rag.documentprocessing.service.chunker.TextChunk;

public interface MetadataGenerator {

    ChunkMetadata generate(Document document, TextChunk chunk);
}
