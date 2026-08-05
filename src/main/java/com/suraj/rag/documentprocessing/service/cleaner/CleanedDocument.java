package com.suraj.rag.documentprocessing.service.cleaner;

import java.util.Map;

public record CleanedDocument(String text, Map<String, Object> metadata) {}
