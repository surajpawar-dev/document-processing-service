package com.suraj.document_processing_service.constants;

public final class ApplicationConstants {

    public static final String API_BASE_PATH = "/documents";
    public static final String PROCESS_PATH = "/process";
    public static final String DOCUMENT_ID_PATH = "/{id}";
    public static final String STATUS_PATH = "/{id}/status";
    public static final String CHUNKS_PATH = "/{id}/chunks";
    public static final String REPROCESS_PATH = "/{id}/reprocess";

    public static final String PDF_CONTENT_TYPE = "application/pdf";

    public static final String STATUS_RECEIVED = "Document processing request received";
    public static final String STATUS_READING = "Reading source document";
    public static final String STATUS_CLEANING = "Cleaning extracted text";
    public static final String STATUS_CHUNKING = "Chunking cleaned text";
    public static final String STATUS_STORING = "Storing chunks";
    public static final String STATUS_READY = "Document is ready for downstream consumption";
    public static final String STATUS_FAILED = "Document processing failed";

    public static final int DEFAULT_CHUNK_PAGE_SIZE = 50;
    public static final int MAX_CHUNK_PAGE_SIZE = 200;
    public static final String CHUNK_ORDER_FIELD = "chunkOrder";

    private ApplicationConstants() {
    }
}
