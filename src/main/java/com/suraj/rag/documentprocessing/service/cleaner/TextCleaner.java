package com.suraj.rag.documentprocessing.service.cleaner;

import com.suraj.rag.documentprocessing.service.reader.ReadDocument;

public interface TextCleaner {

    CleanedDocument clean(ReadDocument document);
}
