package com.suraj.document_processing_service.service.cleaner;

import com.suraj.document_processing_service.service.reader.ReadDocument;

public interface TextCleaner {

    CleanedDocument clean(ReadDocument document);
}
