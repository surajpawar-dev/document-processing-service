package com.suraj.document_processing_service.service.cleaner;

import com.suraj.document_processing_service.exception.CleaningException;
import com.suraj.document_processing_service.service.reader.ReadDocument;
import org.springframework.stereotype.Component;

@Component
public class DefaultTextCleaner implements TextCleaner {

    @Override
    public CleanedDocument clean(ReadDocument document) {
        try {
            // TODO remove extra whitespace, normalize line endings, remove repeated headers/footers,
            // TODO remove page numbers, and drop empty pages.
            return new CleanedDocument(document.text(), document.metadata());
        } catch (RuntimeException ex) {
            throw new CleaningException("Unable to clean extracted text", ex);
        }
    }
}
