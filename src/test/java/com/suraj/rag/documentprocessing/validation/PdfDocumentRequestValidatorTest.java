package com.suraj.rag.documentprocessing.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.suraj.rag.documentprocessing.dto.ProcessDocumentRequest;
import com.suraj.rag.documentprocessing.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PdfDocumentRequestValidatorTest {

    private static final String VALID_CHECKSUM = "a".repeat(64);

    private final PdfDocumentRequestValidator validator = new PdfDocumentRequestValidator();

    @Test
    void acceptsValidPdfRequest() {
        var request =
                new ProcessDocumentRequest(
                        "contract.pdf",
                        "document-processing-prod",
                        "incoming/contract.pdf",
                        VALID_CHECKSUM,
                        "en");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void rejectsNonPdfDocuments() {
        var request =
                new ProcessDocumentRequest(
                        "contract.docx",
                        "document-processing-prod",
                        "incoming/contract.docx",
                        VALID_CHECKSUM,
                        "en");

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsInvalidChecksum() {
        var request =
                new ProcessDocumentRequest(
                        "contract.pdf",
                        "document-processing-prod",
                        "incoming/contract.pdf",
                        "not-a-sha256",
                        "en");

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsUnsafeS3Key() {
        var request =
                new ProcessDocumentRequest(
                        "contract.pdf",
                        "document-processing-prod",
                        "../contract.pdf",
                        VALID_CHECKSUM,
                        "en");

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
}
