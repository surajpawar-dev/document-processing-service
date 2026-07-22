package com.suraj.document_processing_service.service.chunker;

import com.suraj.document_processing_service.enums.ChunkingStrategyType;
import com.suraj.document_processing_service.service.cleaner.CleanedDocument;
import java.util.List;

public interface ChunkingStrategy {

    ChunkingStrategyType type();

    List<TextChunk> chunk(CleanedDocument document);
}
