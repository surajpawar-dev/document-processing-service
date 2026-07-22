package com.suraj.document_processing_service.service.chunker;

import com.suraj.document_processing_service.enums.ChunkingStrategyType;
import com.suraj.document_processing_service.exception.ChunkingException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ChunkingStrategyResolver {

    private final Map<ChunkingStrategyType, ChunkingStrategy> strategies;

    public ChunkingStrategyResolver(List<ChunkingStrategy> strategies) {
        this.strategies = new EnumMap<>(ChunkingStrategyType.class);
        strategies.forEach(strategy -> this.strategies.put(strategy.type(), strategy));
    }

    public ChunkingStrategy resolve(ChunkingStrategyType type) {
        var strategy = strategies.get(type);
        if (strategy == null) {
            throw new ChunkingException("No chunking strategy registered for " + type);
        }
        return strategy;
    }
}
