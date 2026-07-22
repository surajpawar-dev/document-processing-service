package com.suraj.document_processing_service.repository;

import com.suraj.document_processing_service.entity.DocumentProcessingHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingHistoryRepository extends JpaRepository<DocumentProcessingHistory, UUID> {

    List<DocumentProcessingHistory> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
