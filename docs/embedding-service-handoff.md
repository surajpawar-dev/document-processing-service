# Embedding Service Handoff

This document explains what the Document Processing Service produces and what the Embedding Service should consume, store, and expose.

The goal is to keep both services aligned in structure while preserving single responsibility:

- Document Processing Service: read, clean, chunk, and persist document chunks.
- Embedding Service: consume ready chunks, generate embeddings, and store/search vectors.

## Service Boundary

The Document Processing Service is responsible for preparing document text for retrieval pipelines.

It does:

- accepts PDF processing requests
- reads source PDFs from S3
- extracts text with PDFBox
- cleans PDF text noise
- chunks text using configured strategies
- stores document and chunk records
- tracks processing status
- emits a document-ready event hook

It does not:

- call Ollama
- generate embeddings
- store vectors
- perform similarity search
- manage vector indexes
- answer user questions

The Embedding Service should own those responsibilities.

## Current Processing Output

When a document is successfully processed, the document reaches:

```text
READY
```

At that point, chunks are available through:

```http
GET /documents/{documentId}/chunks?page=0&size=50
```

Chunk responses are paginated and ordered by `chunkOrder`.

Example chunk response shape:

```json
{
  "content": [
    {
      "id": "8bb7c7ab-0df1-4975-8dc6-640f73db5ae9",
      "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
      "chunkOrder": 0,
      "content": "The extracted and cleaned chunk text...",
      "checksum": "1f1c0d3a...",
      "pageNumber": 1,
      "section": null,
      "title": null,
      "language": "en",
      "source": "incoming/example.pdf",
      "parentChunkId": null,
      "metadata": {
        "strategy": "RECURSIVE",
        "charStart": 0,
        "charEnd": 998,
        "length": 998
      },
      "createdAt": "2026-08-03T14:30:00Z"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Recommended Integration Contract

The Embedding Service should consume document-ready notifications and then fetch chunks from the Document Processing Service.

Recommended event payload:

```json
{
  "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
  "checksum": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "chunkCount": 42,
  "readyAt": "2026-08-03T14:30:00Z"
}
```

Minimum required fields:

- `documentId`
- `checksum`
- `chunkCount`
- `readyAt`

The Embedding Service should treat `documentId + chunkId + checksum` as the idempotency basis.

## Embedding Service Responsibilities

The Embedding Service should:

1. Receive a document-ready job or event.
2. Fetch document metadata:

```http
GET /documents/{documentId}
```

3. Fetch all chunks with pagination:

```http
GET /documents/{documentId}/chunks?page=0&size=200
```

4. Generate embeddings for each chunk using Ollama.
5. Store vectors and metadata in a vector database.
6. Mark embedding status as completed or failed in its own database.
7. Expose search/retrieval APIs for downstream RAG services.

The Embedding Service should not modify document chunks in the Document Processing Service.

## Suggested Embedding Service Architecture

Keep the same clean structure:

```text
controller
service
worker
client
repository
entity
dto
mapper
properties
exception
constants
config
validation
```

Suggested package responsibilities:

- `controller`: embedding status and vector search APIs
- `service`: orchestration interfaces and implementations
- `worker`: background embedding worker
- `client.document`: client for Document Processing Service
- `client.ollama`: Ollama embedding client
- `repository`: embedding job/vector metadata repositories
- `entity`: embedding job, embedded chunk metadata
- `dto`: request/response/event DTOs
- `properties`: typed config for Ollama, document service, vector store, and processing
- `exception`: domain exceptions and global handler
- `config`: HTTP client, async executor, vector DB clients
- `validation`: API and job validation

## Suggested Embedding Flow

```text
DocumentReadyEvent
  -> create EmbeddingJob(RECEIVED)
  -> fetch chunks page by page
  -> for each chunk:
       generate embedding with Ollama
       store vector with metadata
  -> mark EmbeddingJob READY
```

On failure:

```text
EmbeddingJob FAILED
failureReason saved
retryCount incremented
```

## Ollama Integration

Use Ollama behind an interface. Do not call Ollama directly from controllers or repositories.

Suggested interface:

```java
public interface EmbeddingClient {
    EmbeddingVector embed(String input);
}
```

Suggested implementation:

```java
@Component
class OllamaEmbeddingClient implements EmbeddingClient {
    // calls Ollama /api/embeddings or configured embedding endpoint
}
```

Configuration should be environment-driven:

```yaml
app:
  ollama:
    base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
    embedding-model: ${OLLAMA_EMBEDDING_MODEL:nomic-embed-text}
    request-timeout: ${OLLAMA_REQUEST_TIMEOUT:PT30S}
```

Do not hardcode the model in Java code. Keep it configurable because model choice affects vector dimension, retrieval quality, latency, and storage schema.

Common local embedding model choices include:

- `nomic-embed-text`
- `mxbai-embed-large`
- `all-minilm`

Pick one model for the environment and keep it stable once vectors are stored. If the model changes, old vectors usually need to be regenerated because dimensions and vector space can change.

## Vector Store

The Embedding Service should own vector storage.

Good options:

- PostgreSQL with `pgvector`
- Qdrant
- Milvus
- Weaviate
- Elasticsearch/OpenSearch vector search

For this project, `pgvector` is a pragmatic first choice if PostgreSQL is already part of the platform.

Recommended vector metadata:

```json
{
  "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
  "chunkId": "8bb7c7ab-0df1-4975-8dc6-640f73db5ae9",
  "chunkOrder": 0,
  "documentChecksum": "aaaaaaaa...",
  "chunkChecksum": "1f1c0d3a...",
  "source": "incoming/example.pdf",
  "language": "en",
  "pageNumber": 1,
  "embeddingModel": "nomic-embed-text",
  "embeddingDimension": 768
}
```

Recommended uniqueness:

```text
unique(documentId, chunkId, embeddingModel)
```

This allows re-embedding with a newer model without corrupting existing vectors.

## API Suggestions for Embedding Service

Minimum useful APIs:

```http
POST /embeddings/documents/{documentId}
```

Manually start or retry embedding for a document.

```http
GET /embeddings/documents/{documentId}/status
```

Return embedding job status.

```http
POST /embeddings/search
```

Generate an embedding for the query and return nearest chunks.

Example search request:

```json
{
  "query": "What are the cancellation terms?",
  "topK": 5,
  "documentIds": [
    "bde09e5d-608d-43ad-9048-6dce424fcad0"
  ]
}
```

Example search response:

```json
{
  "matches": [
    {
      "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
      "chunkId": "8bb7c7ab-0df1-4975-8dc6-640f73db5ae9",
      "chunkOrder": 0,
      "content": "The matching chunk text...",
      "score": 0.86,
      "metadata": {
        "pageNumber": 1,
        "source": "incoming/example.pdf"
      }
    }
  ]
}
```

## Status Model

Suggested embedding statuses:

```text
RECEIVED
FETCHING_CHUNKS
EMBEDDING
STORING
READY
FAILED
```

Optional statuses:

```text
PARTIALLY_READY
RETRYING
SKIPPED
```

Start simple with the required statuses.

## Idempotency Rules

The Embedding Service must be idempotent.

If it receives the same document-ready event twice:

- do not duplicate vectors
- reuse or update the existing embedding job
- upsert vectors by `documentId + chunkId + embeddingModel`

If a document is reprocessed:

- chunk IDs may change
- chunk checksums may change
- old vectors for that document/model should be deleted or marked inactive before new vectors become active

Recommended production behavior:

1. Create a new embedding job version.
2. Embed all current chunks.
3. Switch active vectors atomically after success.
4. Retain old vectors temporarily for rollback/debugging.

## Broker Guidance

Keep broker dependency outside business logic.

Recommended design:

```java
public interface EmbeddingJobPublisher {
    void publish(UUID documentId);
}
```

Use one implementation now:

```text
SQS
```

That is enough. Do not add Kafka/RabbitMQ abstractions beyond the interface unless the platform actually needs them.

For local development:

- use an in-memory publisher/listener
- or expose a manual `POST /embeddings/documents/{documentId}` endpoint

For production:

- use SQS
- configure dead-letter queue
- configure visibility timeout longer than max embedding batch time
- process messages idempotently

## Error Handling

Use the same style as the Document Processing Service:

- domain exceptions
- `GlobalExceptionHandler`
- Problem Details responses
- structured logs
- generic external messages for unexpected 500s

Do not expose Ollama internal errors directly to clients. Log them internally and return a clean failure reason.

## Observability

Track at least:

- embedding jobs accepted
- embedding jobs completed
- embedding jobs failed
- embeddings generated
- Ollama latency
- vector store write latency
- vector search latency
- queue depth
- retry count
- failures by reason

## Important Contract Notes

- The Document Processing Service chunk `content` is the source of truth for embedding input.
- The Embedding Service should not re-clean or re-chunk text.
- The same embedding model must be used for indexing and searching.
- Model name and vector dimension must be stored with each vector.
- Changing the embedding model requires re-indexing.
- Pagination must be used when fetching chunks.
- `READY` in Document Processing Service means chunks are available, not that embeddings exist.

## Recommended First Implementation Scope

Build the Embedding Service in this order:

1. Entities and repositories for embedding jobs and embedded chunk metadata.
2. Typed properties for Document Processing Service, Ollama, vector store, and async processing.
3. Document Processing Service client.
4. Ollama embedding client.
5. Vector store adapter.
6. Embedding worker.
7. Manual embedding API.
8. SQS listener/publisher.
9. Search API.
10. Integration tests.

