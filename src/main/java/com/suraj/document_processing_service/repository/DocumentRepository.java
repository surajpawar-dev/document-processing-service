package com.suraj.document_processing_service.repository;

import com.suraj.document_processing_service.entity.Document;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByChecksum(String checksum);
}
