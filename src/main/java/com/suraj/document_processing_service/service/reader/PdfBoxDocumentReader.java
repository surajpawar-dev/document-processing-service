package com.suraj.document_processing_service.service.reader;

import com.suraj.document_processing_service.exception.DocumentReadException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PdfBoxDocumentReader implements DocumentReader {

    private static final Logger log = LoggerFactory.getLogger(PdfBoxDocumentReader.class);

    @Override
    public ReadDocument read(InputStream inputStream, DocumentReadContext context) {
        try (var document = Loader.loadPDF(inputStream.readAllBytes())) {
            log.info("Extracting PDF text sourceBucket={} sourceKey={}", context.sourceBucket(), context.sourceKey());
            var stripper = new PDFTextStripper();
            var text = stripper.getText(document);
            return new ReadDocument(
                    text,
                    List.of(),
                    Map.of("pageCount", document.getNumberOfPages())
            );
        } catch (Exception ex) {
            throw new DocumentReadException("Unable to extract text from PDF", ex);
        }
    }
}
