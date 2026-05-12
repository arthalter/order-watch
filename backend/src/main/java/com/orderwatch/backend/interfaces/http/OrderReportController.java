package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.api.ErrorCode;
import com.orderwatch.backend.application.monitoring.ReportAgent;
import com.orderwatch.backend.interfaces.http.dto.OrderReportResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderReportController {

    private final ReportAgent reportAgent;

    public OrderReportController(ReportAgent reportAgent) {
        this.reportAgent = reportAgent;
    }

    @PostMapping(
            value = {"/api/order_anomaly_report", "/api/order_anomaly_monitor"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse<OrderReportResponse> generateReport() {
        try {
            return ApiResponse.ok(new OrderReportResponse(reportAgent.generateMarkdownReport()));
        } catch (Exception ex) {
            return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }
}
