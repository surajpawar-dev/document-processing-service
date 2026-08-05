package com.suraj.rag.documentprocessing.repository;

import com.suraj.rag.documentprocessing.entity.Document;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByChecksum(String checksum);
}
