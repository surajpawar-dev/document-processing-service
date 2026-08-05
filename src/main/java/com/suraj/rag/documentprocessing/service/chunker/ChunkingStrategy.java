package com.suraj.rag.documentprocessing.service.chunker;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import com.suraj.rag.documentprocessing.service.cleaner.CleanedDocument;
import java.util.List;

public interface ChunkingStrategy {

    ChunkingStrategyType type();

    List<TextChunk> chunk(CleanedDocument document);
}
