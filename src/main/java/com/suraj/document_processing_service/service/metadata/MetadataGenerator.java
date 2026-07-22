package com.suraj.document_processing_service.service.metadata;

import com.suraj.document_processing_service.entity.Document;
import com.suraj.document_processing_service.service.chunker.TextChunk;

public interface MetadataGenerator {

    ChunkMetadata generate(Document document, TextChunk chunk);
}
