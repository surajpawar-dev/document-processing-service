package com.suraj.rag.documentprocessing.service.cleaner;

import static org.assertj.core.api.Assertions.assertThat;

import com.suraj.rag.documentprocessing.service.reader.ReadDocument;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultTextCleanerTest {

    private final DefaultTextCleaner cleaner = new DefaultTextCleaner();

    @Test
    void normalizesPdfTextNoise() {
        var document =
                new ReadDocument(
                        "  First\t line  \r\n\r\n1\r\n\f Page 2 \n\n\nSecond   line  ",
                        List.of(),
                        Map.of("pageCount", 2));

        var cleaned = cleaner.clean(document);

        assertThat(cleaned.text()).isEqualTo("First line\n\nSecond line");
        assertThat(cleaned.metadata())
                .containsEntry("pageCount", 2)
                .containsEntry("cleanedCharacterCount", 23);
    }
}
