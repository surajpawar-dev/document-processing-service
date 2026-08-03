package com.suraj.document_processing_service.repository;

import com.suraj.document_processing_service.entity.DocumentChunk;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    Page<DocumentChunk> findByDocumentId(UUID documentId, Pageable pageable);

    void deleteByDocumentId(UUID documentId);
}
