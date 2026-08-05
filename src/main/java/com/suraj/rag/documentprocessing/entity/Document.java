package com.suraj.rag.documentprocessing.entity;

import com.suraj.rag.documentprocessing.enums.DocumentProcessingStatus;
import com.suraj.rag.documentprocessing.enums.DocumentSourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentSourceType source = DocumentSourceType.S3;

    @Column(nullable = false)
    private String sourceBucket;

    @Column(nullable = false)
    private String sourceKey;

    @Column(nullable = false, unique = true)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentProcessingStatus processingStatus = DocumentProcessingStatus.RECEIVED;

    private String language;

    private Integer pageCount;

    private Integer chunkCount;

    @Column(length = 2048)
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentChunk> chunks = new ArrayList<>();
}
