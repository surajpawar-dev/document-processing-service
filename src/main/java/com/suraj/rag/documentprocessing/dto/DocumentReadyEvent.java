package com.suraj.rag.documentprocessing.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentReadyEvent(
        UUID documentId, String checksum, Integer chunkCount, Instant readyAt) {}
