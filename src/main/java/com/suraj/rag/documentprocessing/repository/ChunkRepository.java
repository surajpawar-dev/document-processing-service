package com.suraj.rag.documentprocessing.repository;

import com.suraj.rag.documentprocessing.entity.DocumentChunk;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    Page<DocumentChunk> findByDocumentId(UUID documentId, Pageable pageable);

    void deleteByDocumentId(UUID documentId);
}
