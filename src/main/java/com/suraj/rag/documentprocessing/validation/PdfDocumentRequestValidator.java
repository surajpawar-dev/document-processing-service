package com.suraj.rag.documentprocessing.validation;

import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PdfDocumentRequestValidator {

    private static final Pattern SHA_256_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");
    private static final Pattern S3_BUCKET_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$");
    private static final String PDF_EXTENSION = ".pdf";

    public void validate(ProcessDocumentRequest request) {
        if (request == null) {
            throw new ValidationException("Document processing request is required");
        }
        if (!StringUtils.endsWithIgnoreCase(request.fileName(), PDF_EXTENSION)) {
            throw new ValidationException("Only PDF documents are supported");
        }
        if (!SHA_256_PATTERN.matcher(StringUtils.defaultString(request.checksum())).matches()) {
            throw new ValidationException("Checksum must be a SHA-256 hex digest");
        }
        if (!S3_BUCKET_PATTERN.matcher(StringUtils.defaultString(request.s3Bucket())).matches()
                || request.s3Bucket().contains("..")
                || request.s3Bucket().matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            throw new ValidationException("S3 bucket name is invalid");
        }
        if (StringUtils.startsWith(request.s3Key(), "/") || request.s3Key().contains("..")) {
            throw new ValidationException("S3 object key is invalid");
        }
    }
}
