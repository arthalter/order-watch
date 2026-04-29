package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.api.ApiResponse;
import com.orderwatch.backend.infrastructure.milvus.MilvusProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.net.Socket;

@RestController
public class HealthController {

    private final MilvusProperties milvusProperties;

    public HealthController(MilvusProperties milvusProperties) {
        this.milvusProperties = milvusProperties;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ok");
    }

    @GetMapping(value = "/health/milvus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<MilvusHealth> milvusHealth() {
        long startedAt = System.currentTimeMillis();
        boolean connected = canConnect(milvusProperties.getHost(), milvusProperties.getPort(), 200);
        long latencyMs = System.currentTimeMillis() - startedAt;
        return ApiResponse.ok(new MilvusHealth(connected, milvusProperties.getHost(), milvusProperties.getPort(), latencyMs));
    }

    private static boolean canConnect(String host, int port, int timeoutMs) {
        if (host == null || host.isBlank() || port <= 0) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    record MilvusHealth(boolean connected, String host, int port, long latencyMs) {
    }
}
