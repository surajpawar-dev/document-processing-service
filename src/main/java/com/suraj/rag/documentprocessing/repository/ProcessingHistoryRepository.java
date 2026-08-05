package com.suraj.rag.documentprocessing.repository;

import com.suraj.rag.documentprocessing.entity.DocumentProcessingHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingHistoryRepository extends JpaRepository<DocumentProcessingHistory, UUID> {

    List<DocumentProcessingHistory> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
