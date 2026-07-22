package com.suraj.document_processing_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProcessDocumentRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 512) String s3Bucket,
        @NotBlank @Size(max = 1024) String s3Key,
        @NotBlank @Size(max = 128) String checksum,
        @Size(max = 32) String language
) {
}
