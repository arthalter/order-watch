package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.api.ErrorCode;
import com.orderwatch.backend.application.chat.OpsChatService;
import com.orderwatch.backend.interfaces.http.dto.OpsChatRequest;
import com.orderwatch.backend.interfaces.http.dto.OpsChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpsChatController {

    private final OpsChatService opsChatService;

    public OpsChatController(OpsChatService opsChatService) {
        this.opsChatService = opsChatService;
    }

    @PostMapping(value = "/api/ops_chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<OpsChatResponse> chat(@RequestBody OpsChatRequest request) {
        try {
            String question = request == null ? null : request.question();
            return ApiResponse.ok(opsChatService.chat(question));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.ok(new OpsChatResponse(false, null, ex.getMessage()));
        }
    }
}
