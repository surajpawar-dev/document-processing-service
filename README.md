# Document Processing Service

Spring Boot service for ingesting PDF documents from S3, extracting text, cleaning it, splitting it into retrieval-friendly chunks, storing document/chunk metadata in PostgreSQL, and publishing a document-ready event hook for downstream RAG or search pipelines.

This service does not generate embeddings, store vectors, call an LLM, or perform retrieval. It prepares clean, traceable chunks so another service can embed and index them.

## Current Status

The core service is implemented and verified with unit tests.

Implemented:

- PDF processing request API.
- Background document processing using a bounded async executor.
- S3 document read abstraction.
- PDFBox text extraction.
- Text normalization and PDF noise cleanup.
- Recursive, sentence, and fixed-size chunking strategies.
- Chunk metadata and checksum generation.
- PostgreSQL persistence with JPA entities.
- Processing status history.
- Paginated chunk retrieval.
- Problem Details error responses.
- Structured application logging.
- Environment-based configuration.
- Production profile requiring real datasource and S3 values.

Still needed before running as a high-scale production platform:

- Replace the no-op event publisher with Kafka, SQS, SNS, RabbitMQ, or another platform event transport.
- Add retry and stuck-job reconciliation for documents left in `READING`, `CLEANING`, `CHUNKING`, or `STORING`.
- Add database migrations with Flyway or Liquibase.
- Add integration tests using PostgreSQL/Testcontainers and real PDF samples.
- Add object-size validation from S3 metadata before downloading large PDFs.
- Add observability dashboards and alerts for failure rate, queue depth, latency, and chunk counts.
- Add authentication/authorization and tenant isolation if this becomes multi-tenant.

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Maven
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- AWS SDK v2 for S3
- Apache PDFBox
- Actuator
- Lombok

## Architecture

The service follows a layered design:

```text
Controller -> DocumentProcessingService -> DocumentProcessingWorker
           -> S3 client
           -> DocumentReader
           -> TextCleaner
           -> ChunkingStrategy
           -> MetadataGenerator
           -> DocumentStorageService
           -> Repositories
```

Important abstractions:

- `DocumentProcessingService`: public application service used by the controller.
- `DocumentProcessingWorker`: asynchronous processing worker.
- `S3DocumentClient`: storage-source abstraction.
- `DocumentReader`: reads source content into extracted text.
- `TextCleaner`: normalizes extracted text.
- `ChunkingStrategy`: strategy interface for chunk splitting.
- `MetadataGenerator`: generates chunk-level metadata and checksums.
- `DocumentStorageService`: persistence orchestration.
- `DocumentEventPublisher`: outbound event abstraction.

## Processing Flow

1. Client sends `POST /documents/process`.
2. Service validates the request.
3. A `Document` record is created with status `RECEIVED`.
4. API returns `202 Accepted` with the document location.
5. Background worker reads the PDF from S3.
6. PDFBox extracts text and page metadata.
7. Text cleaner normalizes line endings, whitespace, form feeds, blank lines, and standalone page numbers.
8. Configured chunking strategy creates chunks.
9. Chunks are stored with order, checksum, source, language, and metadata.
10. Document status becomes `READY`.
11. `DocumentEventPublisher` is called.

If any processing step fails, the document is marked `FAILED` and the failure reason is stored.

## REST API

### Submit a PDF for processing

```http
POST /documents/process
Content-Type: application/json
```

Request:

```json
{
  "fileName": "example.pdf",
  "s3Bucket": "my-document-bucket",
  "s3Key": "incoming/example.pdf",
  "checksum": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "language": "en"
}
```

Response:

- Status: `202 Accepted`
- Header: `Location: /documents/{id}`
- Body: document metadata with initial status.

### Get document metadata

```http
GET /documents/{id}
```

Returns the document record, source metadata, status, page count, chunk count, timestamps, and failure reason when applicable.

### Get document status

```http
GET /documents/{id}/status
```

Use this endpoint to poll after submitting a document.

Statuses:

- `RECEIVED`
- `READING`
- `CLEANING`
- `CHUNKING`
- `METADATA_GENERATION`
- `STORING`
- `READY`
- `FAILED`

### Get chunks

```http
GET /documents/{id}/chunks?page=0&size=50
```

Returns chunks in `chunkOrder` ascending order.

Pagination:

- Default size: `50`
- Maximum size: `200`

### Reprocess a document

```http
POST /documents/{id}/reprocess
```

Resets the existing document, deletes old chunks, and starts background processing again using the same source bucket/key/checksum.

## Configuration

Default configuration lives in `src/main/resources/application.yml`.

Main environment variables:

```text
SPRING_PROFILES_ACTIVE=local
DATABASE_URL=jdbc:postgresql://localhost:5432/document_processing?options=-c%20TimeZone=UTC
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
AWS_REGION=us-east-1
AWS_S3_BUCKET=documents
AWS_S3_ENDPOINT=
AWS_S3_PATH_STYLE_ACCESS_ENABLED=false
CHUNKING_STRATEGY=RECURSIVE
CHUNKING_MAX_SIZE=1000
CHUNKING_OVERLAP_SIZE=200
CHUNKING_MIN_SIZE=200
PDF_MAX_FILE_SIZE_BYTES=52428800
PDF_PASSWORD_PROTECTED_SUPPORTED=false
PDF_EXTRACTION_TIMEOUT=PT2M
PROCESSING_CORE_POOL_SIZE=2
PROCESSING_MAX_POOL_SIZE=4
PROCESSING_QUEUE_CAPACITY=100
PROCESSING_THREAD_NAME_PREFIX=document-processing-
```

For `prod`, these must be supplied explicitly:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
AWS_REGION
AWS_S3_BUCKET
```

## Higher Environment Infrastructure

For local platform runs, the parent `document-rag-platform/docker-compose.yml` provides PostgreSQL and LocalStack. The service runs with:

```text
SPRING_PROFILES_ACTIVE=local
```

For dev, staging, or production, run with:

```text
SPRING_PROFILES_ACTIVE=prod
```

Do not use LocalStack endpoint values in higher environments. Leave `AWS_S3_ENDPOINT` and `AWS_SQS_ENDPOINT` empty or unset so the AWS SDK uses real AWS S3 and SQS endpoints for `AWS_REGION`.

### PostgreSQL

Create a dedicated database for document processing state. This service stores documents, chunks, and processing history.

Minimum configuration:

```text
DATABASE_URL=jdbc:postgresql://document-processing-db.prod.internal:5432/document_processing?options=-c%20TimeZone=UTC
DATABASE_USERNAME=<from-secret>
DATABASE_PASSWORD=<from-secret>
```

Production notes:

- Use a managed PostgreSQL service where possible.
- Enable backups and point-in-time recovery.
- Use TLS for database traffic.
- Keep one database/schema per environment.
- Add Flyway or Liquibase before high-scale production rollout so schema changes are controlled.

### Amazon S3

The service reads PDFs uploaded by `rag-upload-service`.

Required configuration:

```text
AWS_REGION=us-east-1
AWS_S3_BUCKET=documents-prod
AWS_S3_ENDPOINT=
AWS_S3_PATH_STYLE_ACCESS_ENABLED=false
```

Required AWS permissions:

```text
s3:GetObject
s3:HeadObject
```

The bucket must be the same bucket used by `rag-upload-service` for the same environment.

### Amazon SQS

When document-ready events should trigger embedding automatically, use the SQS publisher:

```text
DOCUMENT_EVENT_PUBLISHER=sqs
DOCUMENT_READY_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/document-ready-prod
AWS_SQS_ENDPOINT=
```

Required AWS permissions:

```text
sqs:SendMessage
```

Use a dead-letter queue on the consumer side and keep queue names environment-specific, for example:

```text
document-ready-dev
document-ready-staging
document-ready-prod
```

### Service-to-Service Contract

The Embedding Service depends on these endpoints:

```text
GET /documents/{id}
GET /documents/{id}/chunks?page=0&size=200
GET /documents/{id}/status
```

Keep this service reachable from `rag-embedding-service` through an internal service DNS name or internal load balancer.

### Production Example

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

DATABASE_URL=jdbc:postgresql://document-processing-db.prod.internal:5432/document_processing?options=-c%20TimeZone=UTC
DATABASE_USERNAME=<from-secret>
DATABASE_PASSWORD=<from-secret>

AWS_REGION=us-east-1
AWS_S3_BUCKET=documents-prod
AWS_S3_ENDPOINT=
AWS_S3_PATH_STYLE_ACCESS_ENABLED=false

DOCUMENT_EVENT_PUBLISHER=sqs
AWS_SQS_ENDPOINT=
DOCUMENT_READY_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/document-ready-prod

PROCESSING_CORE_POOL_SIZE=4
PROCESSING_MAX_POOL_SIZE=8
PROCESSING_QUEUE_CAPACITY=500
```

AWS credentials should come from the hosting platform IAM role, not from hardcoded access keys.

## Chunking Strategies

The service currently supports three deterministic chunking strategies.

### Recursive

Configured with:

```text
CHUNKING_STRATEGY=RECURSIVE
```

Best default for RAG. It tries larger semantic boundaries first, then falls back to smaller boundaries and fixed windows:

- paragraph breaks
- line breaks
- sentence-like punctuation
- commas
- spaces
- fixed windows

Use this for most business documents, policies, manuals, contracts, and knowledge-base PDFs.

### Sentence

Configured with:

```text
CHUNKING_STRATEGY=SENTENCE
```

Splits by sentence boundaries and merges sentences into configured chunk sizes. Useful when preserving sentence boundaries is more important than preserving larger document sections.

### Fixed

Configured with:

```text
CHUNKING_STRATEGY=FIXED
```

Creates deterministic fixed-size windows with overlap. Useful as a fallback or for benchmarking, but usually weaker for retrieval quality than recursive chunking.

## Do We Need a Model for Chunking?

Not for the current strategies. Recursive, sentence, and fixed chunking are rule-based and production-usable.

A model or embedding service is only needed if you add semantic chunking, where boundaries are chosen based on meaning rather than only text structure. That can improve retrieval quality for complex documents, but it adds latency, cost, operational dependency, and model/version management.

Recommended production path:

1. Keep `RECURSIVE` as the default.
2. Add token-aware chunk sizing for the embedding model used downstream.
3. Add structure-aware PDF extraction for headings, tables, page numbers, and sections.
4. Add semantic chunking only if retrieval evaluation shows rule-based chunking is not enough.

## Run Locally

Start PostgreSQL and provide AWS/S3-compatible credentials through the normal AWS SDK credential chain.

Run tests:

```bash
./mvnw test
```

Run the service:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Health endpoint:

```http
GET /actuator/health
```

## Error Handling

The API returns RFC 9457-style Problem Details responses.

Examples:

- `400 Bad Request`: invalid request body, unsupported file type, invalid checksum, invalid S3 bucket/key.
- `404 Not Found`: document ID does not exist.
- `422 Unprocessable Entity`: processing-specific failure.
- `500 Internal Server Error`: unexpected server error with a generic external message and detailed internal logs.

## Production Readiness Checklist

Before deploying this as a large-platform service, complete these items:

- Add Flyway or Liquibase migrations and disable schema mutation in production.
- Implement a durable queue for processing requests if multiple service instances are used.
- Replace `NoOpDocumentEventPublisher` with a real event publisher.
- Add retry policy with max attempts and dead-letter handling.
- Add stuck-document reconciliation for worker crashes.
- Add S3 object metadata validation before download.
- Enforce tenant and authorization rules.
- Add API idempotency semantics for duplicate checksums and duplicate submissions.
- Add integration tests for controller, database, S3-compatible storage, and real PDFs.
- Add metrics for accepted jobs, failed jobs, processing duration, chunk count, queue saturation, and PDF extraction failures.
- Add dashboards and alerts.
- Add OpenAPI documentation.
