package com.legalwatch.backend.interfaces.http;

import com.legalwatch.backend.api.ApiResponse;
import com.legalwatch.backend.api.ErrorCode;
import com.legalwatch.backend.application.rag.DocumentUploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocumentUploadController {

    private final DocumentUploadService documentUploadService;

    public DocumentUploadController(DocumentUploadService documentUploadService) {
        this.documentUploadService = documentUploadService;
    }

    @PostMapping(
            value = "/api/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse<DocumentUploadService.UploadResult> upload(@RequestPart("file") MultipartFile file) {
        try {
            return ApiResponse.ok(documentUploadService.uploadAndIndex(file));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage());
        }
    }
}
