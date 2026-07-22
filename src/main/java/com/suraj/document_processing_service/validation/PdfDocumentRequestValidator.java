package com.suraj.document_processing_service.validation;

import com.suraj.document_processing_service.dto.ProcessDocumentRequest;
import com.suraj.document_processing_service.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PdfDocumentRequestValidator {

    public void validate(ProcessDocumentRequest request) {
        // TODO add checksum format, content policy, tenant, and source constraints.
        if (!StringUtils.endsWithIgnoreCase(request.fileName(), ".pdf")) {
            throw new ValidationException("Only PDF documents are supported");
        }
    }
}
