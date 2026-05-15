package com.legalwatch.backend.interfaces.http;

import com.legalwatch.backend.api.ApiResponse;
import com.legalwatch.backend.api.ErrorCode;
import com.legalwatch.backend.application.chat.LegalChatService;
import com.legalwatch.backend.interfaces.http.dto.LegalChatRequest;
import com.legalwatch.backend.interfaces.http.dto.LegalChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalChatController {

    private final LegalChatService legalChatService;

    public LegalChatController(LegalChatService legalChatService) {
        this.legalChatService = legalChatService;
    }

    @PostMapping(value = "/api/legal_chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<LegalChatResponse> chat(@RequestBody LegalChatRequest request) {
        try {
            String question = request == null ? null : request.question();
            return ApiResponse.ok(legalChatService.chat(question));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.ok(new LegalChatResponse(false, null, ex.getMessage()));
        }
    }
}
