package com.suraj.rag.documentprocessing.service.chunker;

import com.suraj.rag.documentprocessing.enums.ChunkingStrategyType;
import com.suraj.rag.documentprocessing.exception.ChunkingException;
import com.suraj.rag.documentprocessing.properties.ChunkingProperties;
import com.suraj.rag.documentprocessing.service.cleaner.CleanedDocument;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SentenceChunkingStrategy implements ChunkingStrategy {

    private final ChunkingProperties properties;

    @Override
    public ChunkingStrategyType type() {
        return ChunkingStrategyType.SENTENCE;
    }

    @Override
    public List<TextChunk> chunk(CleanedDocument document) {
        try {
            var text = ChunkingSupport.normalizeText(document.text());
            var sentences = splitSentences(text);
            var chunks = ChunkingSupport.mergeWithOverlap(sentences, properties);
            return ChunkingSupport.toChunks(chunks, text, type());
        } catch (RuntimeException ex) {
            throw new ChunkingException("Unable to chunk document by sentence", ex);
        }
    }

    private List<String> splitSentences(String text) {
        var iterator = BreakIterator.getSentenceInstance(Locale.ROOT);
        iterator.setText(text);

        var sentences = new ArrayList<String>();
        var start = iterator.first();
        for (var end = iterator.next();
                end != BreakIterator.DONE;
                start = end, end = iterator.next()) {
            var sentence = text.substring(start, end).trim();
            if (!sentence.isBlank()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }
}
