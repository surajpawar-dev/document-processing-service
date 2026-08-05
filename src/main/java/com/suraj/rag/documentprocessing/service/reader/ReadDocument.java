package com.suraj.rag.documentprocessing.service.reader;

import java.util.List;
import java.util.Map;

public record ReadDocument(String text, List<ReadPage> pages, Map<String, Object> metadata) {}
