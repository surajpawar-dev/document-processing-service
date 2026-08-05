package com.suraj.rag.documentprocessing.service.reader;

import java.io.InputStream;

public interface DocumentReader {

    ReadDocument read(InputStream inputStream, DocumentReadContext context);
}
