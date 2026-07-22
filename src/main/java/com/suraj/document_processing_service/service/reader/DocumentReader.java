package com.suraj.document_processing_service.service.reader;

import java.io.InputStream;

public interface DocumentReader {

    ReadDocument read(InputStream inputStream, DocumentReadContext context);
}
