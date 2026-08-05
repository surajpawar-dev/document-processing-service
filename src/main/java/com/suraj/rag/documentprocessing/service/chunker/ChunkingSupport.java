package com.suraj.rag.documentprocessing.service.chunker;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import com.suraj.rag.documentprocessing.properties.ChunkingProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ChunkingSupport {

    private ChunkingSupport() {
    }

    static List<TextChunk> toChunks(List<String> parts, String fullText, ChunkingStrategyType strategy) {
        var chunks = new ArrayList<TextChunk>();
        var searchFrom = 0;

        for (String part : parts) {
            var content = normalizeChunk(part);
            if (content.isBlank()) {
                continue;
            }

            var start = fullText.indexOf(content, searchFrom);
            if (start < 0) {
                start = Math.min(searchFrom, fullText.length());
            }

            var end = Math.min(start + content.length(), fullText.length());
            var metadata = new HashMap<String, Object>();
            metadata.put("strategy", strategy.name());
            metadata.put("charStart", start);
            metadata.put("charEnd", end);
            metadata.put("length", content.length());

            chunks.add(new TextChunk(
                    chunks.size(),
                    content,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Map.copyOf(metadata)
            ));
            searchFrom = Math.max(start + 1, end - 1);
        }

        return List.copyOf(chunks);
    }

    static List<String> fixedWindows(String text, ChunkingProperties properties) {
        var maxChunkSize = maxChunkSize(properties);
        var overlapSize = overlapSize(properties, maxChunkSize);
        var parts = new ArrayList<String>();
        var start = 0;

        while (start < text.length()) {
            var end = Math.min(start + maxChunkSize, text.length());
            if (end < text.length()) {
                end = nearestWordBoundary(text, start, end, properties.getMinChunkSize());
            }

            var chunk = normalizeChunk(text.substring(start, end));
            if (!chunk.isBlank()) {
                parts.add(chunk);
            }

            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - overlapSize, start + 1);
        }

        return parts;
    }

    static List<String> mergeWithOverlap(List<String> units, ChunkingProperties properties) {
        var maxChunkSize = maxChunkSize(properties);
        var overlapSize = overlapSize(properties, maxChunkSize);
        var chunks = new ArrayList<String>();
        var current = new StringBuilder();

        for (String unit : units) {
            var normalized = normalizeChunk(unit);
            if (normalized.isBlank()) {
                continue;
            }

            if (normalized.length() > maxChunkSize) {
                flush(current, chunks);
                chunks.addAll(fixedWindows(normalized, properties));
                continue;
            }

            var separatorLength = current.isEmpty() ? 0 : 1;
            if (!current.isEmpty() && current.length() + separatorLength + normalized.length() > maxChunkSize) {
                flush(current, chunks);
                appendOverlap(current, chunks.getLast(), Math.max(0, maxChunkSize - normalized.length() - 1), overlapSize);
            }

            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(normalized);
        }

        flush(current, chunks);
        return List.copyOf(chunks);
    }

    static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t\\x0B\\f ]+", " ")
                .replaceAll(" *\\n *", "\n")
                .trim();
    }

    private static String normalizeChunk(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private static int maxChunkSize(ChunkingProperties properties) {
        return Math.max(1, properties.getMaxChunkSize());
    }

    private static int overlapSize(ChunkingProperties properties, int maxChunkSize) {
        return Math.min(Math.max(0, properties.getOverlapSize()), Math.max(0, maxChunkSize - 1));
    }

    private static int nearestWordBoundary(String text, int start, int requestedEnd, int minChunkSize) {
        var lowerBound = Math.min(requestedEnd, start + Math.max(1, minChunkSize));
        for (var index = requestedEnd; index > lowerBound; index--) {
            if (Character.isWhitespace(text.charAt(index - 1))) {
                return index;
            }
        }
        return requestedEnd;
    }

    private static void appendOverlap(StringBuilder current, String previousChunk, int availableSize, int overlapSize) {
        if (overlapSize == 0 || availableSize == 0 || previousChunk.isBlank()) {
            return;
        }

        var targetOverlap = Math.min(overlapSize, availableSize);
        var start = Math.max(0, previousChunk.length() - targetOverlap);
        while (start > 0 && !Character.isWhitespace(previousChunk.charAt(start - 1))) {
            start--;
        }
        current.append(previousChunk.substring(start).trim());
    }

    private static void flush(StringBuilder current, List<String> chunks) {
        var content = normalizeChunk(current.toString());
        if (!content.isBlank()) {
            chunks.add(content);
        }
        current.setLength(0);
    }
}
