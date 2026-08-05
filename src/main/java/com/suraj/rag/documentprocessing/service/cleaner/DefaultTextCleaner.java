package com.suraj.rag.documentprocessing.service.cleaner;

import com.suraj.rag.documentprocessing.exception.CleaningException;
import com.suraj.rag.documentprocessing.service.reader.ReadDocument;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DefaultTextCleaner implements TextCleaner {

    private static final Pattern WINDOWS_LINE_ENDING = Pattern.compile("\\r\\n?");
    private static final Pattern FORM_FEED = Pattern.compile("\\f");
    private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("[\\t\\x0B ]+");
    private static final Pattern LINE_PADDING = Pattern.compile("(?m)^ +| +$");
    private static final Pattern EXCESSIVE_BLANK_LINES = Pattern.compile("\\n{3,}");
    private static final Pattern STANDALONE_PAGE_NUMBER = Pattern.compile("(?m)^\\s*(?:page\\s*)?\\d+\\s*$", Pattern.CASE_INSENSITIVE);

    @Override
    public CleanedDocument clean(ReadDocument document) {
        try {
            var rawText = document.text() == null ? "" : document.text();
            var normalized = WINDOWS_LINE_ENDING.matcher(rawText).replaceAll("\n");
            normalized = FORM_FEED.matcher(normalized).replaceAll("\n\n");
            normalized = HORIZONTAL_WHITESPACE.matcher(normalized).replaceAll(" ");
            normalized = STANDALONE_PAGE_NUMBER.matcher(normalized).replaceAll("");
            normalized = LINE_PADDING.matcher(normalized).replaceAll("");
            normalized = EXCESSIVE_BLANK_LINES.matcher(normalized).replaceAll("\n\n").trim();

            var metadata = new LinkedHashMap<>(document.metadata());
            metadata.put("originalCharacterCount", rawText.length());
            metadata.put("cleanedCharacterCount", normalized.length());

            return new CleanedDocument(normalized, metadata);
        } catch (RuntimeException ex) {
            throw new CleaningException("Unable to clean extracted text", ex);
        }
    }
}
