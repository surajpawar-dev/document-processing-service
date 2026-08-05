CREATE TABLE documents (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    source VARCHAR(32) NOT NULL,
    source_bucket VARCHAR(255) NOT NULL,
    source_key VARCHAR(1024) NOT NULL,
    checksum VARCHAR(128) NOT NULL UNIQUE,
    processing_status VARCHAR(32) NOT NULL,
    language VARCHAR(32),
    page_count INTEGER,
    chunk_count INTEGER,
    failure_reason VARCHAR(2048),
    metadata JSONB
);

CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_order INTEGER NOT NULL,
    content TEXT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    page_number INTEGER,
    section VARCHAR(255),
    title VARCHAR(255),
    language VARCHAR(32),
    source VARCHAR(1024),
    parent_chunk_id UUID,
    metadata JSONB,
    CONSTRAINT uq_document_chunks_order UNIQUE (document_id, chunk_order)
);

CREATE TABLE document_processing_history (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    message VARCHAR(2048),
    metadata JSONB
);

CREATE INDEX idx_documents_status ON documents(processing_status);
CREATE INDEX idx_documents_checksum ON documents(checksum);
CREATE INDEX idx_document_chunks_document_order ON document_chunks(document_id, chunk_order);
CREATE INDEX idx_document_chunks_checksum ON document_chunks(checksum);
CREATE INDEX idx_document_processing_history_document ON document_processing_history(document_id, created_at DESC);