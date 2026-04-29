package com.orderwatch.backend.infrastructure.milvus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledIfSystemProperty(named = "milvus.it", matches = "true")
class MilvusConnectivityTest {

    @Test
    void milvusPortsReachable() {
        String host = System.getProperty("milvus.host", "localhost");
        int servicePort = Integer.parseInt(System.getProperty("milvus.port", "19530"));
        int opsPort = Integer.parseInt(System.getProperty("milvus.opsPort", "9091"));

        assertDoesNotThrow(() -> canConnect(host, servicePort, Duration.ofSeconds(2)));
        assertDoesNotThrow(() -> canConnect(host, opsPort, Duration.ofSeconds(2)));
    }

    private static void canConnect(String host, int port, Duration timeout) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeout.toMillis());
        }
    }
}

