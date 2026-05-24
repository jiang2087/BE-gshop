package com.example.demo.controllers.rag;

import com.example.demo.rag.document.DocumentIngestionService;
import com.example.demo.rag.document.DocumentParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag/documents")
public class DocumentController {

    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        String traceId = UUID.randomUUID().toString();

        if (file == null || file.isEmpty()) {
            log.warn("[RAG_DOC_PIPELINE][{}][UPLOAD_REJECTED] Empty file request", traceId);
            return ResponseEntity.badRequest().body("File must not be null or empty");
        }

        String filename = file.getOriginalFilename();
        log.info("[RAG_DOC_PIPELINE][{}][UPLOAD_RECEIVED] filename={}", traceId, filename);

        try {
            String content;
            String sourceType;

            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                content = parserService.parsePdf(file);
                sourceType = "PDF";
            } else if (filename != null && filename.toLowerCase().endsWith(".txt")) {
                content = parserService.parseTxt(file);
                sourceType = "TXT";
            } else {
                log.warn("[RAG_DOC_PIPELINE][{}][UPLOAD_REJECTED] Unsupported file format filename={}", traceId, filename);
                return ResponseEntity.badRequest()
                        .body("Unsupported file format. Only PDF and TXT files are allowed.");
            }

            log.info("[RAG_DOC_PIPELINE][{}][PARSE_OK] filename={} sourceType={} chars={}",
                    traceId, filename, sourceType, content.length());

            ingestionService.ingestDocument(traceId, filename, content, sourceType);

            log.info("[RAG_DOC_PIPELINE][{}][ASYNC_DISPATCHED] filename={} chars={}",
                    traceId, filename, content.length());

            return ResponseEntity.accepted()
                    .body("TraceId: " + traceId + ". Document accepted for ingestion: " + filename
                            + ". Processing " + content.length() + " characters in background.");

        } catch (Exception e) {
            log.error("[RAG_DOC_PIPELINE][{}][UPLOAD_FAILED] filename={}", traceId, filename, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process file: " + e.getMessage());
        }
    }
}
