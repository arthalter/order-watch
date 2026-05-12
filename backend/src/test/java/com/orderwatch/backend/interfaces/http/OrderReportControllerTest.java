package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.application.monitoring.ReportAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderReportController.class)
class OrderReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportAgent reportAgent;

    @Test
    void generatesReport() throws Exception {
        when(reportAgent.generateMarkdownReport())
                .thenReturn("# 异常订单监控报告\n\n## 异常概览\n最近 24 小时发现异常订单。");

        mockMvc.perform(post("/api/order_anomaly_report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.report").value("# 异常订单监控报告\n\n## 异常概览\n最近 24 小时发现异常订单。"));
    }

    @Test
    void supportsLegacyMonitorPath() throws Exception {
        when(reportAgent.generateMarkdownReport())
                .thenReturn("# 异常订单监控报告");

        mockMvc.perform(post("/api/order_anomaly_monitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.report").value("# 异常订单监控报告"));
    }
}
