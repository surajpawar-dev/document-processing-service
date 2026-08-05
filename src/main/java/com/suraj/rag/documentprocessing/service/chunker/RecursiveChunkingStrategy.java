package com.suraj.rag.documentprocessing.service.chunker;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import com.suraj.rag.documentprocessing.exception.ChunkingException;
import com.suraj.rag.documentprocessing.properties.ChunkingProperties;
import com.suraj.rag.documentprocessing.service.cleaner.CleanedDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecursiveChunkingStrategy implements ChunkingStrategy {

    private static final List<String> SEPARATORS = List.of("\n\n", "\n", ". ", "; ", ", ", " ", "");

    private final ChunkingProperties properties;

    @Override
    public ChunkingStrategyType type() {
        return ChunkingStrategyType.RECURSIVE;
    }

    @Override
    public List<TextChunk> chunk(CleanedDocument document) {
        try {
            var text = ChunkingSupport.normalizeText(document.text());

            // Prefer semantic boundaries first, then fall back to words and fixed windows for long
            // runs.
            var units = splitRecursively(text, 0);
            var chunks = ChunkingSupport.mergeWithOverlap(units, properties);
            return ChunkingSupport.toChunks(chunks, text, type());
        } catch (RuntimeException ex) {
            throw new ChunkingException("Unable to chunk document recursively", ex);
        }
    }

    private List<String> splitRecursively(String text, int separatorIndex) {
        if (text.length() <= properties.getMaxChunkSize()) {
            return List.of(text);
        }

        if (separatorIndex >= SEPARATORS.size() - 1) {
            return ChunkingSupport.fixedWindows(text, properties);
        }

        var parts = new ArrayList<String>();
        for (String part : splitKeepingSeparator(text, SEPARATORS.get(separatorIndex))) {
            var normalized = part.trim();
            if (normalized.isBlank()) {
                continue;
            }
            parts.addAll(splitRecursively(normalized, separatorIndex + 1));
        }

        return parts;
    }

    private List<String> splitKeepingSeparator(String text, String separator) {
        var parts = new ArrayList<String>();
        var start = 0;
        var next = text.indexOf(separator, start);
        while (next >= 0) {
            var end = next + separator.length();
            parts.add(text.substring(start, end));
            start = end;
            next = text.indexOf(separator, start);
        }

        if (start < text.length()) {
            parts.add(text.substring(start));
        }

        return parts;
    }
}
