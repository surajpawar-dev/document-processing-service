package com.suraj.document_processing_service.repository;

import com.suraj.document_processing_service.entity.DocumentChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByDocumentIdOrderByChunkOrderAsc(UUID documentId);

    void deleteByDocumentId(UUID documentId);
}
