package com.suraj.rag.documentprocessing.service.reader;

import java.util.Map;

public record ReadPage(int pageNumber, String text, Map<String, Object> metadata) {}
